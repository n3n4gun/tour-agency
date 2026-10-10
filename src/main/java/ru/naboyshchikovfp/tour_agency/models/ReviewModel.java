package ru.naboyshchikovfp.tour_agency.models;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reviews")
public class ReviewModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserModel user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", referencedColumnName = "id", nullable = false)
    private TourModel tour;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", referencedColumnName = "id", nullable = false, unique = true)
    private BookingModel booking;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ReviewModel() {}

    public ReviewModel(BookingModel booking, int rating, String text) {
        this.booking = booking;
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException("Отзыв можно оставить только после завершённой поездки");
        }
        this.user = booking.getUser();
        this.tour = booking.getTour();
        this.setRating(rating);
        this.setText(text);
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

    public BookingModel getBooking() {
        return this.booking;
    }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Оценка должна быть от 1 до 5");
        }
        this.rating = rating;
    }

    public int getRating() {
        return this.rating;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

}
