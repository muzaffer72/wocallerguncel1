/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.api;

import com.whocaller.spamdetector.Config;
import com.whocaller.spamdetector.modal.Contact;
import com.whocaller.spamdetector.modal.ResponseWrapper;
import com.whocaller.spamdetector.modal.UserProfile;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Config.BASE_URL+"/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public interface ApiService {
        @GET("api")
        Call<ApiResponse> getApiData();

        @POST("get-name")
        Call<NameResponse> getName(@Body PhoneNumberRequest phoneNumberRequest);

        @POST("save-contact-one")
        Call<Void> postContact(@Body ContactListRequest contacts);

        @FormUrlEncoded
        @POST("getSearchContact-data")
        Call<ResponseWrapper<Object>> getSearchContactData(
                @Field("id") int id,
                @Field("type") String type
        );
        @FormUrlEncoded
        @POST("contact-data")
        Call<ResponseWrapper<Object>> getContactData(@Field("phoneNumber") String phoneNumber);

        @FormUrlEncoded
        @POST("contacts/search")
        Call<List<Contact>> searchContacts(@Field("searchTerm") String searchTerm);
        @Multipart
        @POST("upload")
        Call<ResponseBody> uploadImage(@Part MultipartBody.Part image);
        @FormUrlEncoded
        @POST("contacts")
        Call<List<Contact>> getContacts(@Field("phoneNumber") String phoneNumber);

        @POST("save-contacts")
        Call<Void> postContacts(@Body ContactListRequest contacts);
        @POST("register-phone")
        Call<Void> createUserProfileOnPhone(@Body UserProfile userProfile);
        @POST("register-email")
        Call<Void> createUserProfileOnEmail(@Body UserProfile userProfile);

        @POST("register-google")
        Call<Void> createUserProfileOnGoogle(@Body UserProfile userProfile);
        @POST("get-profile")
        Call<UserProfile.UserProfileResponse> getProfileDetails(@Body UserProfile userProfile);
        @POST("update-profile")
        Call<Void> updateProfile(@Body UserProfile userProfile);

    }

    public static class NameResponse {
        private String name;
        private String type;


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class PhoneNumberRequest {
        private String phoneNumber;

        public PhoneNumberRequest(String phone_number) {
            this.phoneNumber = phone_number;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phone_number) {
            this.phoneNumber = phone_number;
        }
    }


    public static class ContactListRequest {
        private List<Contact> contacts;
        private String appuser_id;

        public ContactListRequest(List<Contact> contacts, String appuser_id) {
            this.contacts = contacts;
            this.appuser_id = appuser_id;
        }

        public List<Contact> getContacts() {
            return contacts;
        }

        public void setContacts(List<Contact> contacts) {
            this.contacts = contacts;
        }

    }

}