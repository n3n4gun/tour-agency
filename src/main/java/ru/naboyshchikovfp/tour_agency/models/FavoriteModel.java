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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity 
@Table(name = "favorites",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tour_id"}))
public class FavoriteModel {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private TourModel tour;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    protected FavoriteModel() {}

    public FavoriteModel(UserModel user, TourModel tour) {
        this.setUser(user);
        this.setTour(tour);
    }

    public Long getId() {
        return this.id;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public UserModel getUser() {
        return this.user;
    }

    public void setTour(TourModel tour) {
        this.tour = tour;
    }

    public TourModel getTour() {
        return this.tour;
    }

    public LocalDateTime getAddedAt() {
        return this.addedAt;
    }

}
