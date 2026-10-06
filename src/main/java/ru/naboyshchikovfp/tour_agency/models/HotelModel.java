package ru.naboyshchikovfp.tour_agency.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "hotels")
public class HotelModel {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city_name", nullable = false, length = 150)
    private String cityName;

    @Column(name = "hotel_name", nullable = false, unique = true, length = 100)
    private String hotelName;

    @Column(name = "stars", nullable = false)
    private int stars;

    @Column(name = "address", nullable = false, unique = true, length = 300)
    private String address;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public HotelModel(String cityName, String hotelName, int stars, String address, String description) {
        this.setCityName(cityName);
        this.setHotelName(hotelName);
        this.setStars(stars);
        this.setAddress(address);
        this.setDescription(description);
    }

    public Long getId() {
        return this.id;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCityName() {
        return this.cityName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getHotelName() {
        return this.hotelName;
    }

    public void setStars(int stars) {
        if (stars >= 1 || stars <= 5) {
            this.stars = stars;
        }
        else {
            this.stars = 5;
        }
    }

    public int getStars() {
        return this.stars;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return this.address;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

}
