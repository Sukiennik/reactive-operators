package pl.edu.agh.sukiennik.thesis.assesments.model;

import java.util.List;

public class Book {

    public String name;
    public List<Chapter> chapters;

    public Book(String name, List<Chapter> chapters) {
        this.name = name;
        this.chapters = chapters;
    }
}
