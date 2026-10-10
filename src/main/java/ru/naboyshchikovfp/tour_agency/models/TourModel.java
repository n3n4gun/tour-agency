package ru.naboyshchikovfp.tour_agency.models;

import java.math.BigDecimal;

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

    @Column(name = "tour_name", nullable = false, length = 100)
    private String tourName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "tour_type_name", nullable = false, length = 50)
    private String tourTypeName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", referencedColumnName = "id", nullable = false)
    private HotelModel hotel;

    @Column(name = "price_per_adult_person", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerAdultPerson;

    @Column(name = "price_per_child_person", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerChildPerson;

    @Column(name = "duration_nights", nullable = false)
    private int durationNights;

    protected TourModel() {}

    public TourModel(String tourName, String description, String tourTypeName, HotelModel hotel, BigDecimal pricePerAdultPerson, BigDecimal pricePerChildPerson, int durationNights) {
        this.setTourName(tourName);
        this.setDescription(description);
        this.setTourTypeName(tourTypeName);
        this.setHotel(hotel);
        this.setPricePerAdultPerson(pricePerAdultPerson);
        this.setPricePerChildPerson(pricePerChildPerson);
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

    public void setPricePerAdultPerson(BigDecimal pricePerAdultPerson) {
        if (pricePerAdultPerson == null || pricePerAdultPerson.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Стоимость взрослого билета за одну единицу должна быть больше 0");
        }
        this.pricePerAdultPerson = pricePerAdultPerson;
    }

    public BigDecimal getPricePerAdultPerson() {
        return this.pricePerAdultPerson;
    }

    public void setPricePerChildPerson(BigDecimal pricePerChildPerson) {
        if (pricePerChildPerson == null || pricePerChildPerson.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Стоимость детского билета за одну единицу начинается от 0 и больше");
        }
        this.pricePerChildPerson = pricePerChildPerson;
    }

    public BigDecimal getPricePerChildPerson() {
        return this.pricePerChildPerson;
    }

    public void setDurationNights(int durationNights) {
        if (durationNights <= 0) {
            throw new IllegalArgumentException("Минимально допустимое количество ночей составляет 1");
        }
        this.durationNights = durationNights;
    }

    public int getDurationNights() {
        return this.durationNights;
    }

}
