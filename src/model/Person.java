package model;



/**
 * Abstract superclass of everyone the league deals with. It holds the personal data
 * shared by players, coaches, officials, the commissioner and spectators.
 */
public abstract class Person {
    protected final String id;
    protected String name;
    protected int age;
    protected String gender;

    protected Person(String id, String name, int age, String gender) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    

    public Person() {
        this.id = "000";
        this.name ="N/A";
        this.age = 0;
        this.gender = "N/A";
    }

    public Person(Person p){
        this.id = p.id;
        this.name = p.name;
        this.age = p.age;
        this.gender = p.gender;
    }

    



    /** The role shown to users, such as "Player". Every subclass must supply it. */
    public abstract String getRole();

    /** One line describing this person. Subclasses can extend it. */
    public String describe() {
        return getRole() + " " + id + ": " + name + " (age " + age + ", " + gender + ")";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name =name; 
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return describe();
    }
}



