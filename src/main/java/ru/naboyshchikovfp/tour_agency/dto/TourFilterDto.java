package ru.naboyshchikovfp.tour_agency.dto;

import java.math.BigDecimal;

// заполняется из GET-параметров каталога, например /tours?cityName=Анталья&maxPrice=100000
// все поля необязательные: null означает, что фильтр по этому полю не применяется
public class TourFilterDto {
    private String cityName;

    private String tourTypeName;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer minNights;

    private Integer maxNights;

    public TourFilterDto() {}

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
    public String getCityName() { return this.cityName; }

    public void setTourTypeName(String tourTypeName) {
        this.tourTypeName = tourTypeName;
    }
    public String getTourTypeName() { return this.tourTypeName; }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }
    public BigDecimal getMinPrice() { return this.minPrice; }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }
    public BigDecimal getMaxPrice() { return this.maxPrice; }

    public void setMinNights(Integer minNights) {
        this.minNights = minNights;
    }
    public Integer getMinNights() { return this.minNights; }

    public void setMaxNights(Integer maxNights) {
        this.maxNights = maxNights;
    }
    public Integer getMaxNights() { return this.maxNights; }

}
