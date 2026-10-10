package ru.naboyshchikovfp.tour_agency.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;

@Entity 
@Table(name = "bookings")
public class BookingModel {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserModel user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", referencedColumnName = "id", nullable = false)
    private TourModel tour;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "adults_count", nullable = false)
    private int adultsCount;

    @Column(name = "children_count", nullable = false)
    private int childrenCount;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status = BookingStatus.NEW;

    protected BookingModel() {}

    public BookingModel(UserModel user, TourModel tour, LocalDate departureDate, int adultsCount, int childrenCount) {
        this.user = user;
        this.tour = tour;
        this.setDepartureDate(departureDate);
        this.setAdultsCount(adultsCount);
        this.setChildrenCount(childrenCount);
        this.totalPrice = tour.getPricePerAdultPerson().multiply(BigDecimal.valueOf(adultsCount))
            .add(tour.getPricePerChildPerson().multiply(BigDecimal.valueOf(childrenCount)));
    }

    public Long getId() {
        return this.id;
    }

    public UserModel getUser() {
        return this.user;
    }

    public TourModel getTour() {
        return this.tour;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDate getDepartureDate() {
        return this.departureDate;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setAdultsCount(int adultsCount) {
        if (adultsCount <= 0) {
            throw new IllegalArgumentException("Количество взрослых лиц должно быть строго больше 0");
        }
        this.adultsCount = adultsCount;
    }

    public int getAdultsCount() {
        return this.adultsCount;
    }

    public void setChildrenCount(int childrenCount) {
        if (childrenCount < 0) {
            throw new IllegalArgumentException("Минимальное количество детских билетов составляет 0 единиц");
        }
        this.childrenCount = childrenCount;
    }

    public int getChildrenCount() {
        return this.childrenCount;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return this.totalPrice;
    }

    public BookingStatus getStatus() {
        return this.status;
    }

}
