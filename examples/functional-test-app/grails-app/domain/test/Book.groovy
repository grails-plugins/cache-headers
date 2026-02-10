package test

class Book {
    String name
    Date dateCreated
    Date lastUpdated

    static mapping = {
        table 'books'
    }
}