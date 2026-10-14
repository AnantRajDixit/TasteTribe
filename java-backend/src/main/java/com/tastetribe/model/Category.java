package com.tastetribe.model;

/** A recipe category (Breakfast, Desserts, Vegan, ...). */
public class Category extends BaseEntity {

    private String name;
    private String slug;
    private String description;
    private String imageUrl;

    public Category() {
        super();
    }

    public Category(String id, java.time.LocalDateTime createdAt) {
        super(id, createdAt);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
