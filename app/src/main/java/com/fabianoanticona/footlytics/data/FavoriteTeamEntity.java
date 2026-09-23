package com.fabianoanticona.footlytics.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_teams")
public class FavoriteTeamEntity {

    @PrimaryKey
    private int id;
    private String name;
    private String country;
    private String logo;

    public FavoriteTeamEntity(int id, String name, String country, String logo) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.logo = logo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
