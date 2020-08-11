package pl.edu.agh.sukiennik.thesis.assesments.model;

import java.util.List;

public class Chapter {

    public int id;
    public String title;
    public List<Image> images;

    public Chapter(int id, String title, List<Image> images) {
        this.id = id;
        this.title = title;
        this.images = images;
    }
}
