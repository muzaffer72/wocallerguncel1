/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.modal;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Contact implements Parcelable {
    private String name;
    private String phoneNumber;
    private boolean isWho;
    private boolean isSpam;
    private String spamType;
    private String tag;
    private String carrierName;
    private String countryName;
    private String contactsBy;
    private int id;
    private String type;

    // Yeni eklenen alan (Profil resmi URL)
    private String imageUrl; // İster sunucu URL'si, ister yerel path saklayabilirsiniz.

    // Örnek: Eski constructor + imageUrl alanını destekleyen bir constructor
    public Contact(String name, String phoneNumber, boolean isWho, boolean isSpam, String spamType,
                   String tag, String carrierName, String countryName, String contactsBy) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.isWho = isWho;
        this.isSpam = isSpam;
        this.spamType = spamType;
        this.tag = tag;
        this.carrierName = carrierName;
        this.countryName = countryName;
        this.contactsBy = contactsBy;
    }

    // Opsiyonel: imageUrl dahil bir constructor
    public Contact(String name, String phoneNumber, boolean isWho, boolean isSpam, String spamType,
                   String tag, String carrierName, String countryName, String contactsBy, String imageUrl) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.isWho = isWho;
        this.isSpam = isSpam;
        this.spamType = spamType;
        this.tag = tag;
        this.carrierName = carrierName;
        this.countryName = countryName;
        this.contactsBy = contactsBy;
        this.imageUrl = imageUrl;
    }

    public Contact() {
        // Boş constructor
    }

    // Getter / Setter Metodları
    public String getSpamType() {
        return spamType;
    }

    public void setSpamType(String spamType) {
        this.spamType = spamType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isWho() {
        return isWho;
    }

    public void setIsWho(boolean isWho) {
        this.isWho = isWho;
    }

    public boolean isSpam() {
        return isSpam;
    }

    public void setIsSpam(boolean spam) {
        isSpam = spam;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getContactsBy() {
        return contactsBy;
    }

    public void setContactsBy(String contactsBy) {
        this.contactsBy = contactsBy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Yeni eklenen getter/setter
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // -----------------
    // Parcel işlemleri
    protected Contact(Parcel in) {
        id = in.readInt();
        type = in.readString();
        name = in.readString();
        phoneNumber = in.readString();
        isWho = in.readByte() != 0;
        isSpam = in.readByte() != 0;
        spamType = in.readString();
        tag = in.readString();
        carrierName = in.readString();
        countryName = in.readString();
        // imageUrl alanını da ekliyoruz
        imageUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Write parcel
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(type);
        dest.writeString(name);
        dest.writeString(phoneNumber);
        dest.writeByte((byte) (isWho ? 1 : 0));
        dest.writeByte((byte) (isSpam ? 1 : 0));
        dest.writeString(spamType);
        dest.writeString(tag);
        dest.writeString(carrierName);
        dest.writeString(countryName);
        // imageUrl da parcel'e yazılıyor
        dest.writeString(imageUrl);
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
}
