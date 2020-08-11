package pl.edu.agh.sukiennik.thesis.assesments.model;

import java.util.Objects;

public class Image {
    public int width;
    public int height;
    public String url;

    public Image(int width, int height, String url) {
        this.width = width;
        this.height = height;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return width == image.width &&
                height == image.height &&
                Objects.equals(url, image.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, url);
    }

    @Override
    public String toString() {
        return "BoxArt{" +
                "width=" + width +
                ", height=" + height +
                ", url='" + url + '\'' +
                '}';
    }
}
