package com.waht.platform.vo;

import com.waht.platform.entity.NoteTagEntity;

public class NoteTagResponse {

    private Long id;
    private String name;
    private String slug;
    private String color;

    public static NoteTagResponse from(NoteTagEntity tag) {
        NoteTagResponse response = new NoteTagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        response.setSlug(tag.getSlug());
        response.setColor(tag.getColor());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
