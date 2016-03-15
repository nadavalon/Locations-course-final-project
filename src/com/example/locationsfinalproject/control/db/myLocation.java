package com.example.locationsfinalproject.control.db;

import com.google.android.gms.maps.model.LatLng;

public class myLocation {
	private long id;
	private String name;
	private String address;
	private LatLng location;
	private String icon;
	private double distance;
	private double latitude;
	private double longitude;
	private String lastquery;
	private String lastaction;

	public myLocation(long id, String name, String address, LatLng location,
			String icon, double distance, double latitude, double longitude,
			String lastquery, String lastaction) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.location = location;
		this.icon = icon;
		this.distance = distance;
		this.latitude = latitude;
		this.longitude = longitude;
		this.lastquery = lastquery;
		this.lastaction = lastaction;

	}

	public myLocation(String name, LatLng location) {

		this.name = name;
		this.location = location;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public LatLng getLocation() {
		return location;
	}

	public void setLocation(LatLng location) {
		this.location = location;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getLastQuery() {
		return lastquery;
	}

	public void setLastQuery(String lastquery) {
		this.lastquery = lastquery;
	}

	public String getLastAction() {
		return lastaction;
	}

	public void setLastAction(String lastaction) {
		this.lastaction = lastaction;
	}

	@Override
	public String toString() {
		return "myLocation [id=" + id + ", name=" + name + ", address="
				+ address + ", location=" + location + ", icon=" + icon + ","
				+ ", distance=" + distance + ", latitude=" + latitude
				+ ", longitude=" + longitude + ", lastquery=" + lastquery
				+ ", lastaction=" + lastaction + "]";
	}

}
