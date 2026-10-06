package ru.naboyshchikovfp.tour_agency.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tours")
public class TourModel {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tour_name", nullable = false, unique = true, length = 100)
    private String tourName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "tour_type_name", nullable = false, length = 50)
    private String tourTypeName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", referencedColumnName = "id", nullable = false)
    private HotelModel hotel;

    @Column(name = "duration_nights", nullable = false)
    private int durationNights;

    public TourModel(String tourName, String description, String tourTypeName, HotelModel hotel, int durationNights) {
        this.setTourName(tourName);
        this.setDescription(description);
        this.setTourTypeName(tourTypeName);
        this.setHotel(hotel);
        this.setDurationNights(durationNights);
    }

    public Long getId() {
        return this.id;
    }

    public void setTourName(String tourName) {
        this.tourName = tourName;
    }

    public String getTourName() {
        return this.tourName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setTourTypeName(String tourTypeName) {
        this.tourTypeName = tourTypeName;
    }

    public String getTourTypeName() {
        return this.tourTypeName;
    }

    public void setHotel(HotelModel hotel) {
        this.hotel = hotel;
    }

    public HotelModel getHotel() {
        return this.hotel;
    }

    public void setDurationNights(int durationNights) {
        if (durationNights > 0) {
            this.durationNights = durationNights;
        }
        else {
            this.durationNights = 1;
        }
    }

    public int getDurationNights() {
        return this.durationNights;
    }

}
