package io.requery.test.model;


import io.requery.Entity;
import io.requery.Generated;
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.PostInsert;
import io.requery.PostLoad;
import io.requery.PreUpdate;
import io.requery.PropertyNameStyle;
import io.requery.Table;
import io.requery.Transient;
import io.requery.Version;
import io.requery.query.MutableResult;

@Entity(propertyNameStyle = PropertyNameStyle.FLUENT_BEAN)
@Table(name = "Groups")
public class AbstractGroup {

    @Key @Generated
    protected int id;

    protected String name;
    protected String description;
    protected GroupType type;
    protected byte[] picture;

    @Version
    protected int version;

    /*
    @JunctionTable(columns = {
            @Column(name = "personId", foreignKey =
                @ForeignKey(references = AbstractPerson.class, referencedColumn = "id")),
            @Column(name = "groupId", foreignKey =
                @ForeignKey(references = AbstractGroup.class, referencedColumn = "id")) } )
    */
    @JunctionTable
    @ManyToMany
    protected MutableResult<Person> persons;

    //@Column("CURRENT_DATE")
    protected java.sql.Date createdDate;

    //@Column("CURRENT_TIMESTAMP")
    //protected java.sql.Timestamp createdTimestamp;

    @Transient
    protected String temporaryName;

    @PostInsert
    @PostLoad
    @PreUpdate
    public void combinedListener() {

    }
}
