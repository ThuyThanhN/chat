package com.example.svmarket.dto;

public class FavoriteToggleResponse {
    private Integer listingId;
    private boolean favorited;

    public FavoriteToggleResponse() {
    }

    public FavoriteToggleResponse(Integer listingId, boolean favorited) {
        this.listingId = listingId;
        this.favorited = favorited;
    }

    public Integer getListingId() {
        return listingId;
    }

    public void setListingId(Integer listingId) {
        this.listingId = listingId;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }
}
