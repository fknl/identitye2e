package com.indentitye2e.library.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document("books")
public class Book {

    @Id
    String ISBN;
    String title;
    String author;
    int publicationyear;
    int availablecopies;

    public Book() {
    }

    public Book(String ISBN, String title, String author, int publicationyear, int availablecopies) {
        this.ISBN = ISBN;
        this.title = title;
        this.author = author;
        this.publicationyear = publicationyear;
        this.availablecopies = availablecopies;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getPublicationyear() {
        return publicationyear;
    }

    public void setPublicationyear(int publicationyear) {
        this.publicationyear = publicationyear;
    }

    public int getAvailablecopies() {
        return availablecopies;
    }

    public void setAvailablecopies(int availablecopies) {
        this.availablecopies = availablecopies;
    }
}
