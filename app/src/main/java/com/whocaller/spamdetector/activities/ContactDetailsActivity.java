/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.activities;

import static com.whocaller.spamdetector.utils.Utils.getContactImage;
import static com.whocaller.spamdetector.utils.Utils.getLookupKeyFromPhoneNumber;
import static com.whocaller.spamdetector.utils.Utils.isContactStarred;
import static com.whocaller.spamdetector.utils.Utils.isPhoneNumberSaved;
import static com.whocaller.spamdetector.utils.Utils.isValidName;
import static com.whocaller.spamdetector.utils.Utils.makeCall;
import static com.whocaller.spamdetector.utils.Utils.showToast;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.naliya.callerid.database.prefs.AdsPrefHelper;
import com.naliya.callerid.database.prefs.ProfilePrefHelper;
import com.whocaller.spamdetector.Config;
import com.whocaller.spamdetector.R;
import com.whocaller.spamdetector.adapter.CallLogAdapter;
import com.whocaller.spamdetector.adapter.TagAdapter;
import com.whocaller.spamdetector.ads.Ads;
import com.whocaller.spamdetector.database.sqlite.BlockCallerDbHelper;
import com.whocaller.spamdetector.database.sqlite.ContactsDataDb;
import com.whocaller.spamdetector.databinding.ActivityContactDetailsBinding;
import com.whocaller.spamdetector.helpers.CallLogHelper;
import com.whocaller.spamdetector.modal.CallLogItem;
import com.whocaller.spamdetector.modal.Contact;
import com.whocaller.spamdetector.utils.PhoneNumberUtils;
import com.whocaller.spamdetector.utils.Utils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ContactDetailsActivity extends AppCompatActivity {

    String phoneNumber, phoneNumberNationalFormat, contactName, lookupKey, FormatPhoneNumber, contactBy, profileTypeName, spamType;
    private boolean isStarred;

    ActivityContactDetailsBinding binding;
    private BlockCallerDbHelper blockCallerDbHelper;
    boolean isBlock = false;
    boolean isSpam = false;
    boolean isWhoProfile = false;

    Window window;
    ContactsDataDb contactsDataDb;
    ProfilePrefHelper profilePrefHelper;
    String TAGS = null;
    Contact contactModal;

    AdsPrefHelper adsPrefHelper;
    private List<Contact> contactList = new ArrayList<>();

    // -------------------------------
    // LiveData Değişkenleri
    private MutableLiveData<String> contactNameLiveData = new MutableLiveData<>();
    private MutableLiveData<String> phoneNumberLiveData = new MutableLiveData<>();
    private MutableLiveData<String> carrierNameLiveData = new MutableLiveData<>();
    private MutableLiveData<String> countryNameLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isBlockLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isSpamLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isWhoProfileLiveData = new MutableLiveData<>();
    private MutableLiveData<String> tagLiveData = new MutableLiveData<>();
    private MutableLiveData<String> spamTypeLiveData = new MutableLiveData<>();
    // -------------------------------

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Back pressed callback
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Utils.reDirectMainActivity(ContactDetailsActivity.this);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        blockCallerDbHelper = new BlockCallerDbHelper(this);
        contactsDataDb = new ContactsDataDb(this);
        profilePrefHelper = new ProfilePrefHelper(ContactDetailsActivity.this);
        adsPrefHelper = new AdsPrefHelper(this);

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String regionCode = getRegionCode(this);

        if (Utils.isNetworkAvailable(this)) {
            Ads ads = new Ads(this);
            ads.loadingNativeAd(this);
        }
        String phone_number;
        Intent intent = getIntent();
        if (intent != null) {
            phone_number = intent.getStringExtra("phoneNumber");
            contactName = intent.getStringExtra("name");
            isBlock = intent.getBooleanExtra("isBlock", false);
            isSpam = intent.getBooleanExtra("isSpam", false);

            if (intent.getStringExtra("whocaller") != null) {
                contactBy = intent.getStringExtra("whocaller");
                contactByText();
            }

            isWhoProfile = intent.getBooleanExtra("whoprofile", false);

            if (intent.getParcelableExtra("modal") != null) {
                contactModal = intent.getParcelableExtra("modal");
                phone_number = contactModal.getPhoneNumber();
                contactName = contactModal.getName();
                TAGS = contactModal.getTag();
                isSpam = contactModal.isSpam();
                spamType = contactModal.getSpamType();
                isWhoProfile = contactModal.isWho();

                if (contactModal.getContactsBy() != null) {
                    contactBy = contactModal.getContactsBy();
                    contactByText();
                }
            }

            phoneNumber = phone_number;
            if (contactName == null) {
                contactName = "Whocaller user";
            }

            String nationalFormat = PhoneNumberUtils.toNationalFormat(phone_number.replace("*", "").replace("#", ""), "TR");
            if (nationalFormat != null) {
                binding.phoneNumber.setText(nationalFormat);
                phoneNumberNationalFormat = nationalFormat;
            } else {
                binding.phoneNumber.setText(phone_number.replace("*", "").replace("#", ""));
            }

            // LiveData setValue çağrıları
            contactNameLiveData.setValue(contactName);
            phoneNumberLiveData.setValue(phoneNumber);
            if (contactModal != null) {
                carrierNameLiveData.setValue(contactModal.getCarrierName());
                countryNameLiveData.setValue(contactModal.getCountryName());
                tagLiveData.setValue(contactModal.getTag());
                spamTypeLiveData.setValue(contactModal.getSpamType());
            }
            isBlockLiveData.setValue(isBlock);
            isSpamLiveData.setValue(isSpam);
            isWhoProfileLiveData.setValue(isWhoProfile);

            binding.contactName.setText(Utils.toTextCase(contactName));

            lookupKey = getLookupKeyFromPhoneNumber(phoneNumber, this);

            isStarred = isContactStarred(phoneNumber, this);
            simulateIconChange();

            Contact contact = contactsDataDb.getContactByPhoneNumber(phoneNumber);
            if (contact != null) {
                contactModal = contact;
                contactName = contact.getName();
                isSpam = contact.isSpam();
                TAGS = contact.getTag();
                spamType = contact.getSpamType();
                isWhoProfile = contact.isWho();
                if (contact.getContactsBy() != null) {
                    contactBy = contact.getContactsBy();
                }
                // Güncellenen verileri LiveData'ya aktaralım
                contactNameLiveData.setValue(contact.getName());
                phoneNumberLiveData.setValue(contact.getPhoneNumber());
                carrierNameLiveData.setValue(contact.getCarrierName());
                countryNameLiveData.setValue(contact.getCountryName());
                tagLiveData.setValue(contact.getTag());
                spamTypeLiveData.setValue(contact.getSpamType());
                isWhoProfileLiveData.setValue(contact.isWho());
            }

            FormatPhoneNumber = phoneNumber.replace("*", "").replace("#", "");
            try {
                Phonenumber.PhoneNumber phoneNumberr = phoneNumberUtil.parse(FormatPhoneNumber, regionCode);
                if (phoneNumberUtil.isValidNumber(phoneNumberr)) {
                    FormatPhoneNumber = phoneNumberUtil.format(phoneNumberr, PhoneNumberUtil.PhoneNumberFormat.E164);
                }
            } catch (NumberParseException e) {
                throw new RuntimeException(e);
            }

            checkWhatsAppContact(FormatPhoneNumber);
            tagsAndNameChange();

            if (isWhoProfile) {
                binding.whoProfile.setImageDrawable(getDrawable(R.drawable.who));
            }

            if (contactBy != null) {
                if (contactBy.equals("whocaller")) {
                    binding.suggestLay.setVisibility(View.VISIBLE);
                    binding.editName.setVisibility(View.VISIBLE);
                }
            }

            window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            layoutChangers();
            contactByText();
            binding.profileTitleName.setText(profileTypeName);

            binding.favoriteBtn.setOnClickListener(v -> toggleStarStatus());
            binding.ivMenu.setOnClickListener(ContactDetailsActivity.this::showPopupMenu);
            binding.block.setOnClickListener(v -> showConfirmBlockDialog());

            binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));

            List<CallLogItem> callLogItems = CallLogHelper.getCallLogsForNumber(this, phoneNumber);

            if (callLogItems.size() > 0) {
                CallLogAdapter callLogAdapter = new CallLogAdapter(callLogItems, this, true, false);
                callLogAdapter.setShowHeader(true);
                binding.recyclerview.setAdapter(callLogAdapter);
                binding.noCallsHistory.setVisibility(View.GONE);
                binding.viewAllLay.setVisibility(View.VISIBLE);
            } else {
                binding.viewAllLay.setVisibility(View.GONE);
                binding.noCallsHistory.setVisibility(View.VISIBLE);
            }

            if (contactModal != null && contactModal.getCarrierName() != null && contactModal.getCountryName() != null) {
                if (!contactModal.getCarrierName().equals("null")) {
                    binding.network.setText(contactModal.getCarrierName());
                    binding.location.setText(contactModal.getCountryName());
                }
            } else {
                lookupCarrier(FormatPhoneNumber, new CarrierLookupCallback() {
                    @Override
                    public void onSuccess(String carrierName, String countryCode) {
                        runOnUiThread(() -> {
                            String countryName = new Locale("", countryCode).getDisplayCountry();
                            if (carrierName != null && !carrierName.equals("null")) {
                                binding.network.setText(carrierName);
                                carrierNameLiveData.setValue(carrierName);
                            } else {
                                binding.network.setText(getString(R.string.mobile));
                                carrierNameLiveData.setValue(getString(R.string.mobile));
                            }
                            binding.location.setText(countryName);
                            countryNameLiveData.setValue(countryName);

                            // DB güncellemesi
                            Contact contactx = new Contact();
                            contactx.setName(contactName);
                            contactx.setPhoneNumber(phoneNumber);
                            contactx.setIsWho(isWhoProfile);
                            contactx.setIsSpam(isSpam);
                            contactx.setSpamType(spamType);
                            contactx.setTag(TAGS);
                            contactx.setCountryName(countryName);
                            contactx.setContactsBy(contactBy);

                            contactsDataDb.addContactOrUpdate(contactx);
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() -> binding.network.setText(getString(R.string.mobile)));
                    }
                });
            }

            if (isPhoneNumberSaved(phoneNumber, this)) {
                binding.saveLay.setVisibility(View.GONE);
                binding.edit.setOnClickListener(v -> {
                    if (lookupKey == null) {
                        Utils.openContactEditPage(this, getLookupKeyFromPhoneNumber(phoneNumber, this));
                    } else {
                        Utils.openContactEditPage(this, lookupKey);
                    }
                });
            } else {
                binding.editLay.setVisibility(View.GONE);
                binding.save.setOnClickListener(v -> Utils.openContactCreatePage(this, phoneNumber, contactName));
            }

            binding.ivBack.setOnClickListener(v -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });

            binding.callBtn.setOnClickListener(v -> makeCall(phoneNumber, this));
            binding.callBtn2.setOnClickListener(v -> makeCall(phoneNumber, this));

            binding.viewAll.setOnClickListener(v -> {
                Intent intents = new Intent(this, CallHistoryActivity.class);
                intents.putExtra("name", contactName);
                intents.putExtra("phoneNumber", phoneNumber);
                startActivity(intents);
            });

            binding.editName.setOnClickListener(v -> showEditNameBottomSheet());
            binding.suggestName.setOnClickListener(v -> showEditNameBottomSheet());
            binding.addTagName.setOnClickListener(v -> showAddTagBottomSheet());

            binding.whatsappLay.setOnClickListener(v -> {
                Uri uri = Uri.parse("https://wa.me/" + FormatPhoneNumber);
                Intent intents = new Intent(Intent.ACTION_VIEW, uri);
                intents.setPackage("com.whatsapp");
                startActivity(intents);
            });
        }
    }

    // ----------------------------
    // LiveData ile yönetilebilecek metodlar:
    private void updateProfileName(String fName, String lName) {
        // Eğer gerekirse LiveData üzerinden de güncelleme yapabilirsiniz.
        // Bu örnekte contactNameLiveData zaten set ediliyor.
    }

    // Diğer orijinal metotlar (contactByText, checkWhatsAppContact, tagsAndNameChange, layoutChangers, copyContactToClipboard, toggleStarStatus, getRegionCode, lookupCarrier, CarrierLookupCallback, simulateIconChange) aynen kalıyor.
    // ...
    private void contactByText() {
        if (contactBy != null) {
            if (contactBy.equals("whocaller")) {
                profileTypeName = "IDENTIFIED BY " + getResources().getString(R.string.app_name);
            }
        } else {
            profileTypeName = getResources().getString(R.string.in_your_contacts);
        }
    }

    private void checkWhatsAppContact(String contactNumber) {
        Uri uri = Uri.parse("https://wa.me/" + contactNumber);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.whatsapp");
        if (intent.resolveActivity(getPackageManager()) != null) {
            binding.whatsappLay.setVisibility(View.VISIBLE);
        }
    }

    private void tagsAndNameChange() {
        if (TAGS != null && !TAGS.equals("null") && !TAGS.isEmpty()) {
            binding.tagLay.setVisibility(View.VISIBLE);
            binding.tagName.setText(TAGS);
            binding.tagIcon.setImageDrawable(TagAdapter.getIcon(this, TAGS));
        } else {
            binding.tagLay.setVisibility(View.GONE);
        }
    }

    private void showAddTagBottomSheet() {
        String[] tagArray = getResources().getStringArray(R.array.tags_list);
        List<String> tags = Arrays.asList(tagArray);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        @SuppressLint("InflateParams") View bottomSheetView = getLayoutInflater().inflate(R.layout.add_tag_bottom_layout, null);

        TagAdapter tagAdapter = new TagAdapter(ContactDetailsActivity.this, tags, tag -> {
            TAGS = tag;
            Toast.makeText(ContactDetailsActivity.this, getString(R.string.selected) + " " + tag, Toast.LENGTH_SHORT).show();
            tagLiveData.setValue(tag);
        });

        RecyclerView recyclerView = bottomSheetView.findViewById(R.id.tagRecyclerView);
        ImageView saveBtn = bottomSheetView.findViewById(R.id.save_btn);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(tagAdapter);

        saveBtn.setOnClickListener(v -> {
            if (TAGS != null) {
                Contact contact = new Contact();
                contact.setName(contactName);
                contact.setPhoneNumber(phoneNumber);
                contact.setIsWho(isWhoProfile);
                contact.setIsSpam(isSpam);
                contact.setSpamType(spamType);
                contact.setTag(TAGS);
                contact.setContactsBy(contactBy);

                contactsDataDb.addContactOrUpdate(contact);
                contactList.add(contact);
                if (isValidName(contactName) && !isWhoProfile) {
                    Utils.postContact(contactList, profilePrefHelper.getPhone());
                }
                bottomSheetDialog.cancel();
                tagsAndNameChange();
                showToast(ContactDetailsActivity.this, getString(R.string.toast_improving));
            } else {
                bottomSheetDialog.cancel();
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void showEditNameBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ContactDetailsActivity.this);
        @SuppressLint("InflateParams") View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.edit_name_bottom_layout, null);

        ImageView closeButton = bottomSheetView.findViewById(R.id.close_btn);
        TextView saveBtn = bottomSheetView.findViewById(R.id.save);
        EditText editText = bottomSheetView.findViewById(R.id.edit_text);
        RadioGroup radioGroup = bottomSheetView.findViewById(R.id.radio_group);

        closeButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        saveBtn.setOnClickListener(v -> {
            if (!editText.getText().toString().isEmpty()) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                if (selectedId != -1) {
                    if (selectedId == R.id.radio_business) {
                        spamType = "1";
                    } else if (selectedId == R.id.radio_person) {
                        spamType = "0";
                    }
                    Contact contact = new Contact();
                    contact.setName(editText.getText().toString());
                    contact.setPhoneNumber(phoneNumber);
                    contact.setIsWho(isWhoProfile);
                    contact.setIsSpam(isSpam);
                    contact.setSpamType(spamType);
                    contact.setTag(TAGS);
                    contact.setContactsBy(contactBy);

                    contactsDataDb.addContactOrUpdate(contact);
                    contactList.add(contact);
                    if (!isWhoProfile) {
                        Utils.postContact(contactList, profilePrefHelper.getPhone());
                    }
                    bottomSheetDialog.dismiss();
                    binding.contactName.setText(Utils.toTextCase(editText.getText().toString()));
                    tagsAndNameChange();
                    showToast(ContactDetailsActivity.this, getString(R.string.toast_improving));
                } else {
                    showToast(ContactDetailsActivity.this, getString(R.string.please_select_an_option));
                }
            } else {
                editText.setError(getString(R.string.please_enter_valid_name));
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.show();
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.contact_details_menu, popupMenu.getMenu());

        Menu menu = popupMenu.getMenu();
        if (isPhoneNumberSaved(phoneNumber, this)) {
            MenuItem saveItem = menu.findItem(R.id.save);
            saveItem.setVisible(false);
        } else {
            MenuItem editItem = menu.findItem(R.id.edit);
            editItem.setVisible(false);
        }

        if (isWhoProfile) {
            MenuItem report_contact = menu.findItem(R.id.report_contact);
            report_contact.setVisible(false);
            MenuItem revoke_report = menu.findItem(R.id.revoke_report);
            revoke_report.setVisible(false);
        } else {
            if (isSpam) {
                MenuItem report_contact = menu.findItem(R.id.report_contact);
                report_contact.setVisible(false);
                MenuItem revoke_report = menu.findItem(R.id.revoke_report);
                if (revoke_report != null) {
                    SpannableString s = new SpannableString(revoke_report.getTitle());
                    s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red, null)), 0, s.length(), 0);
                    revoke_report.setTitle(s);
                }
            } else {
                MenuItem revoke_report = menu.findItem(R.id.revoke_report);
                revoke_report.setVisible(false);
                MenuItem report_contact = menu.findItem(R.id.report_contact);
                if (report_contact != null) {
                    SpannableString s = new SpannableString(report_contact.getTitle());
                    s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red, null)), 0, s.length(), 0);
                    report_contact.setTitle(s);
                }
            }
        }

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.share) {
                Utils.shareContact(ContactDetailsActivity.this, contactName, phoneNumber);
                return true;
            } else if (id == R.id.edit) {
                if (lookupKey == null) {
                    Utils.openContactEditPage(this, getLookupKeyFromPhoneNumber(phoneNumber, this));
                } else {
                    Utils.openContactEditPage(this, lookupKey);
                }
                return true;
            } else if (id == R.id.save) {
                Utils.openContactCreatePage(this, phoneNumber, contactName);
                return true;
            } else if (id == R.id.copy_name) {
                Utils.copyToClipboard(ContactDetailsActivity.this, contactName, "name");
                showToast(ContactDetailsActivity.this, getString(R.string.copy_name_succeeded));
                return true;
            } else if (id == R.id.copy_number) {
                Utils.copyToClipboard(ContactDetailsActivity.this, phoneNumber, "number");
                showToast(ContactDetailsActivity.this, getString(R.string.copy_number_succeeded));
                return true;
            } else if (id == R.id.copy_contact) {
                copyContactToClipboard(contactName, phoneNumber);
                showToast(ContactDetailsActivity.this, getString(R.string.copy_contact_succeeded));
                return true;
            } else if (id == R.id.report_contact) {
                new AlertDialog.Builder(ContactDetailsActivity.this)
                        .setTitle(getString(R.string.report_contact_title))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(getString(R.string.are_you_sure_report_contact))
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            Contact contact = new Contact();
                            contact.setName(contactName);
                            contact.setPhoneNumber(phoneNumber);
                            contact.setIsWho(isWhoProfile);
                            contact.setIsSpam(true);
                            contact.setSpamType(spamType);
                            contact.setTag(TAGS);
                            contact.setContactsBy(contactBy);
                            contactsDataDb.addContactOrUpdate(contact);
                            contactList.add(contact);
                            if (isValidName(contactName) && !isWhoProfile) {
                                Utils.postContact(contactList, profilePrefHelper.getPhone());
                            }
                            showToast(ContactDetailsActivity.this, getString(R.string.toast_improving));
                            layoutChangers();
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;
            } else if (id == R.id.revoke_report) {
                new AlertDialog.Builder(ContactDetailsActivity.this)
                        .setTitle(getString(R.string.revoke_report_title))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(getString(R.string.are_you_sure_revoke_report))
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            Contact contact = new Contact();
                            contact.setName(contactName);
                            contact.setPhoneNumber(phoneNumber);
                            contact.setIsWho(isWhoProfile);
                            contact.setIsSpam(false);
                            contact.setSpamType(spamType);
                            contact.setTag(TAGS);
                            contact.setContactsBy(contactBy);
                            contactsDataDb.addContactOrUpdate(contact);
                            contactList.add(contact);
                            if (isValidName(contactName) && !isWhoProfile) {
                                Utils.postContact(contactList, profilePrefHelper.getPhone());
                            }
                            showToast(ContactDetailsActivity.this, getString(R.string.report_revoked_successfully));
                            layoutChangers();
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }

    private void showConfirmBlockDialog() {
        String message;
        String title;
        if (isBlock) {
            message = getString(R.string.do_you_want_to_unblock_contact);
            title = getString(R.string.confirm_unblock);
        } else {
            message = getString(R.string.do_you_want_to_block_contact);
            title = getString(R.string.confirm_block);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(getString(android.R.string.yes), (dialog, id) -> {
                    if (isBlock) {
                        if (blockCallerDbHelper.deleteBlockCallerByPhoneNumber(phoneNumber)) {
                            isBlockLiveData.setValue(false);
                        }
                    } else {
                        if (blockCallerDbHelper.addBlockCaller(contactName, phoneNumber)) {
                            isBlockLiveData.setValue(true);
                        }
                    }
                })
                .setNegativeButton(getString(android.R.string.no), (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void layoutChangers() {
        if (isBlock || isSpam) {
            if (spamType != null && !spamType.equals("null")) {
                if (spamType.equals(Config.BUSINESS)) {
                    binding.typeBusiness.setVisibility(View.VISIBLE);
                } else if (spamType.equals(Config.PERSON)) {
                    binding.typePerson.setVisibility(View.VISIBLE);
                }
            }
            if (isBlock) {
                binding.contactImage.setImageDrawable(getResources().getDrawable(R.drawable.block_circle, null));
            } else if (isSpam) {
                binding.contactImage.setImageDrawable(getResources().getDrawable(R.drawable.spam_circle, null));
            }
        } else {
            binding.typeBusiness.setVisibility(View.GONE);
            binding.typePerson.setVisibility(View.GONE);
            if (isValidName(contactName)) {
                Bitmap contactImage = getContactImage(this, phoneNumber);
                if (contactImage != null) {
                    binding.contactImage.setImageBitmap(contactImage);
                } else {
                    binding.contactImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_avatar, null));
                }
            } else {
                binding.contactImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_avatar, null));
            }
        }
        if (isBlock || isSpam) {
            binding.blockText.setText(isBlock ? getString(R.string.unblock) : getString(R.string.block));
            binding.bgLay.setBackgroundColor(getResources().getColor(R.color.red, null));
            window.setStatusBarColor(getResources().getColor(R.color.red, null));
        } else {
            binding.blockText.setText(getString(R.string.block));
            binding.bgLay.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark, null));
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark, null));
        }
    }

    private void copyContactToClipboard(String name, String number) {
        String contactInfo = getString(R.string.name2) + name + getString(R.string.number2) + number;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Contact Info", contactInfo);
        clipboard.setPrimaryClip(clip);
    }

    private void toggleStarStatus() {
        if (lookupKey != null && !lookupKey.isEmpty()) {
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
            Uri contactUri = ContactsContract.Contacts.lookupContact(getContentResolver(), lookupUri);
            if (contactUri != null) {
                ContentValues values = new ContentValues();
                values.put(ContactsContract.Contacts.STARRED, isStarred ? 0 : 1);
                int rowsUpdated = getContentResolver().update(contactUri, values, null, null);
                if (rowsUpdated > 0) {
                    isStarred = !isStarred;
                    simulateIconChange();
                    String message = isStarred ?
                            String.format(getString(R.string.added_to_favourites), contactName) :
                            String.format(getString(R.string.removed_from_favourites), contactName);
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.failed_to_update_contact), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.contact_not_found), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.please_save_contact), Toast.LENGTH_SHORT).show();
        }
    }

    private String getRegionCode(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String simCountryIso = telephonyManager.getSimCountryIso().toUpperCase();
        if (simCountryIso.isEmpty()) {
            simCountryIso = Locale.getDefault().getCountry();
        }
        return simCountryIso;
    }

    private void lookupCarrier(String phoneNumber, CarrierLookupCallback callback) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://lookups.twilio.com/v1/PhoneNumbers/" + phoneNumber + "?Type=carrier";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", okhttp3.Credentials.basic(getString(R.string.twilid_id), getString(R.string.twilid_auth_token)))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }
            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseBody);
                        JSONObject carrier = jsonObject.getJSONObject("carrier");
                        String carrierName = carrier.getString("name");
                        String countryCode = jsonObject.getString("country_code");
                        callback.onSuccess(carrierName, countryCode);
                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                } else {
                    callback.onFailure(new IOException("Unexpected code " + response));
                }
            }
        });
    }

    interface CarrierLookupCallback {
        void onSuccess(String carrierName, String countryCode);
        void onFailure(Exception e);
    }

    private void simulateIconChange() {
        if (isStarred) {
            binding.ivFav.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary)));
        } else {
            binding.ivFav.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
        }
    }
}
