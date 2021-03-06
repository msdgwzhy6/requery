/*
 * Copyright 2016 requery.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.requery.test;

import io.requery.Persistable;
import io.requery.cache.EntityCacheBuilder;
import io.requery.meta.EntityModel;
import io.requery.query.Result;
import io.requery.rx.SingleEntityStore;
import io.requery.rx.RxSupport;
import io.requery.sql.Configuration;
import io.requery.sql.ConfigurationBuilder;
import io.requery.sql.EntityDataStore;
import io.requery.sql.SchemaModifier;
import io.requery.sql.TableCreationMode;
import io.requery.sql.platform.HSQL;
import io.requery.sql.Platform;
import io.requery.test.model.Person;
import io.requery.test.model.Phone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import javax.sql.CommonDataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RxTest extends RandomData {

    protected SingleEntityStore<Persistable> data;

    @Before
    public void setup() throws SQLException {
        Platform platform = new HSQL();
        CommonDataSource dataSource = DatabaseType.getDataSource(platform);
        EntityModel model = io.requery.test.model.Models.DEFAULT;

        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();
        Configuration configuration = new ConfigurationBuilder(dataSource, model)
            .useDefaultLogging()
            .setWriteExecutor(Executors.newSingleThreadExecutor())
            .setEntityCache(new EntityCacheBuilder(model)
                .useReferenceCache(true)
                .useSerializableCache(true)
                .useCacheManager(cacheManager)
                .build())
            .build();

        SchemaModifier tables = new SchemaModifier(configuration);
        tables.createTables(TableCreationMode.DROP_CREATE);
        data = RxSupport.toReactiveStore(new EntityDataStore<Persistable>(configuration));
    }

    @After
    public void teardown() {
        if (data != null) {
            data.close();
        }
    }

    @Test
    public void testInsert() throws Exception {
        Person person = randomPerson();
        final CountDownLatch latch = new CountDownLatch(1);
        data.insert(person).subscribe(new Action1<Person>() {
            @Override
            public void call(Person person) {
                assertTrue(person.getId() > 0);
                Person cached = data.select(Person.class)
                    .where(Person.ID.equal(person.getId())).get().first();
                assertSame(cached, person);
                latch.countDown();
            }
        });
        latch.await();
    }

    @Test
    public void testDelete() throws Exception {
        Person person = randomPerson();
        data.insert(person).flatMap(new Func1<Person, Single<Void>>() {
            @Override
            public Single<Void> call(Person person) {
                return data.delete(person);
            }
        }).toBlocking().value();
        Person cached = data.select(Person.class)
            .where(Person.ID.equal(person.getId())).get().firstOrNull();
        assertNull(cached);
    }

    @Test
    public void testInsertCount() throws Exception {
        Person person = randomPerson();
        Observable.just(person)
            .concatMap(new Func1<Person, Observable<Person>>() {
            @Override
            public Observable<Person> call(Person person) {
                return data.insert(person).toObservable();
            }
        });
        Person p = data.insert(person).toBlocking().value();
        assertTrue(p.getId() > 0);
        int count = data.count(Person.class).get().toSingle().toBlocking().value();
        assertEquals(1, count);
    }

    @Test
    public void testInsertOneToMany() throws Exception {
        final Person person = randomPerson();
        data.insert(person).map(new Func1<Person, Phone>() {
            @Override
            public Phone call(Person person) {
                Phone phone1 = randomPhone();
                phone1.setOwner(person);
                return phone1;
            }
        }).flatMap(new Func1<Phone, Single<?>>() {
            @Override
            public Single<?> call(Phone phone) {
                return data.insert(phone);
            }
        }).toBlocking().value();
        assertTrue(person.getPhoneNumbers().toList().size() == 1);
    }

    @Test
    public void testQueryObservable() throws Exception {
        for (int i = 0; i < 30; i++) {
            Person person = randomPerson();
            data.insert(person).toBlocking().value();
        }
        final List<Person> people = new ArrayList<>();
        data.select(Person.class).limit(50).get()
            .toObservable()
            .observeOn(Schedulers.newThread())
            .subscribeOn(Schedulers.immediate())
            .subscribe(new Action1<Person>() {
            @Override
            public void call(Person person) {
                people.add(person);
            }
        });
        assertEquals(30, people.size());
    }

    @Test
    public void testQuerySelfObservable() throws Exception {
        final CountDownLatch latch = new CountDownLatch(2);
        data.select(Person.class).get().toSelfObservable().subscribe(
            new Action1<Result<Person>>() {
            @Override
            public void call(Result<Person> persons) {
                latch.countDown();
            }
        });
        data.insert(randomPerson()).toBlocking().value();
        data.insert(randomPerson()).toBlocking().value();
        assertEquals(0L, latch.getCount());
    }

    @Test
    public void testQueryObservableFromEntity() throws Exception {
        final Person person = randomPerson();
        data.insert(person).map(new Func1<Person, Phone>() {
            @Override
            public Phone call(Person person) {
                Phone phone1 = randomPhone();
                phone1.setOwner(person);
                return phone1;
            }
        }).flatMap(new Func1<Phone, Single<?>>() {
            @Override
            public Single<?> call(Phone phone) {
                return data.insert(phone);
            }
        }).toBlocking().value();
        int count = person.getPhoneNumbers().toObservable().count().toBlocking().first();
        assertEquals(1, count);
    }

    @Test
    public void testQueryObservablePull() throws Exception {
        for (int i = 0; i < 36; i++) {
            Person person = randomPerson();
            data.insert(person).toBlocking().value();
        }
        final List<Person> people = new ArrayList<>();
        data.select(Person.class).get()
            .toObservable()
            //.observeOn(Schedulers.newThread())
            .subscribeOn(Schedulers.immediate())
            .subscribe(new Subscriber<Person>() {
                @Override
                public void onStart() {
                    super.onStart();
                    request(10);
                }

                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(Person person) {
                    //System.out.println("t: " + Thread.currentThread().getName());
                    people.add(person);
                    if (people.size() % 10 == 0 && people.size() > 1) {
                        request(10);
                    }
                }
            });
        assertEquals(36, people.size());
    }
}
