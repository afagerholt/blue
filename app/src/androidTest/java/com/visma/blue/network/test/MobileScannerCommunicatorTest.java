package com.visma.blue.network.test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.test.AndroidTestCase;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.visma.blue.network.Base;
import com.visma.blue.network.BlueNetworkError;
import com.visma.blue.network.OnlinePhotoType;
import com.visma.blue.network.VolleySingleton;
import com.visma.blue.network.containers.CreatePhotoAnswer;
import com.visma.blue.network.containers.GetCompaniesAnswer;
import com.visma.blue.network.containers.GetEmailAnswer;
import com.visma.blue.network.containers.GetMetadataListAnswer;
import com.visma.blue.network.containers.GetPhotoAnswer;
import com.visma.blue.network.containers.GetTokenAnswer;
import com.visma.blue.network.containers.OnlineMetaData;
import com.visma.blue.network.containers.OnlinePhoto;
import com.visma.blue.network.requests.CreatePhotoRequest;
import com.visma.blue.network.requests.DeletePhotoRequest;
import com.visma.blue.network.requests.GetCompaniesRequest;
import com.visma.blue.network.requests.GetEmailRequest;
import com.visma.blue.network.requests.GetMetadataListRequest;
import com.visma.blue.network.requests.GetPhotoRequest;
import com.visma.blue.network.requests.GetTokenRequest;
import com.visma.blue.network.requests.UpdatePhotoRequest;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class MobileScannerCommunicatorTest extends AndroidTestCase {
    private static final VolleySingleton.ServerType serverType = VolleySingleton.ServerType.ALPHA;

    public MobileScannerCommunicatorTest() {
        super();
    }

    public static String getUser() {
        return "appteamspcs@gmail.com";
    }

    public static String getPassword() {
        return "abc.123";
    }

    protected int getAppId() {
        return 1110; // SPCS Android
    }

    protected static OnlineMetaData getSimpleMetaData() {
        OnlineMetaData metaData = new OnlineMetaData();

        metaData.canDelete = true;
        metaData.comment = "Simple data created from an automatic test.";
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(2015, 02, 12);
        metaData.date = calendar.getTime();
        metaData.type = OnlinePhotoType.INVOICE.getValue();
        metaData.photoId = "";

        return metaData;
    }

    protected static OnlineMetaData getExtendedMetaData() {
        OnlineMetaData metaData = new OnlineMetaData();

        metaData.canDelete = true;
        metaData.comment = "Extended data created from an automatic test.";
        metaData.type = OnlinePhotoType.INVOICE.getValue();
        metaData.photoId = "";

        metaData.isPaid = true;

        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(2015, 02, 12);

        metaData.date = calendar.getTime();
        calendar.add(Calendar.DATE, -5);
        metaData.invoiceDate = calendar.getTime();
        calendar.add(Calendar.DATE, 25);
        metaData.paymentDate = calendar.getTime();
        calendar.add(Calendar.DATE, 5);
        metaData.dueDate = calendar.getTime();

        metaData.usingQrString = null;
        metaData.name = "Lasses Bradgard AB";
        metaData.organisationNumber = "555555-5555";
        metaData.referenceNumber = "123";
        metaData.dueAmount = 5500.0;
        metaData.highVatAmount = null;
        metaData.middleVatAmount = null;
        metaData.lowVatAmount = null;
        metaData.totalVatAmount = metaData.dueAmount;
        metaData.currency = "SEK";

        return metaData;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Setup volley so that it is configured and ready when we want to use it later on.
        VolleySingleton.getInstance().setAndSaveServer(getContext(), VolleySingleton.getInstance
                ().getServerData(VolleySingleton.ServerType.ALPHA));
    }

    public void testPreconditions() {
        assertEquals(VolleySingleton.getInstance().getServer().url, VolleySingleton.getInstance
                ().getServerData(VolleySingleton.ServerType.ALPHA).url);
    }

    //TODO enable instrumentation tests when problems in backend are solved
   /* public void testDoGetUsers_success() {
        GetCustomers(getUser(), getPassword(), getAppId());
    }

    public void testDoLogin_success() {
        ArrayList<GetCompaniesAnswer.OnlineCustomer> companies = GetCustomers(getUser(),
                getPassword(), getAppId());
        GetToken(getUser(), getPassword(), companies.get(0).companyId);
    }

    public void testDoGetEmail() {
        ArrayList<GetCompaniesAnswer.OnlineCustomer> companies = GetCustomers(getUser(), getPassword(),
                getAppId());
        String token = GetToken(getUser(), getPassword(), companies.get(0).companyId);

        GetInboundEmail(token);
    }

    public void testCreatePhoto_simple() {
        ArrayList<GetCompaniesAnswer.OnlineCustomer> companies = GetCustomers(getUser(),
                getPassword(), getAppId());
        String token = GetToken(getUser(), getPassword(), companies.get(0).companyId);

        String photoId = CreatePhoto(token, null, getSimpleMetaData());

        // Clean up
        DeletePhoto(token, photoId, null);
    }

    public void testCreatePhoto_verifyExistence() {
        ArrayList<GetCompaniesAnswer.OnlineCustomer> companies = GetCustomers(getUser(),
                getPassword(), getAppId());
        String token = GetToken(getUser(), getPassword(), companies.get(0).companyId);

        OnlineMetaData metaData = getExtendedMetaData();
        String photoId = CreatePhoto(token, null, metaData);

        // Verify that it has been created
        ArrayList<OnlineMetaData> metaDataList = GetMetaDataList(token, null);
        boolean photoFound = false;
        for (OnlineMetaData photo : metaDataList) {
            if (photo.photoId.equalsIgnoreCase(photoId)) {
                photoFound = true;
                compareMetaData(metaData, photo);
                break;
            }
        }
        assertTrue("Photo has been uploaded but the meta data list does not include it", photoFound);

        // Clean up
        DeletePhoto(token, photoId, null);
    }

    public void testDeletePhoto_simple() {
        ArrayList<GetCompaniesAnswer.OnlineCustomer> companies = GetCustomers(getUser(),
                getPassword(), getAppId());
        String token = GetToken(getUser(), getPassword(), companies.get(0).companyId);

        String photoId = CreatePhoto(token, null, getSimpleMetaData());

        DeletePhoto(token, photoId, null);
    }

    public void testDeletePhoto_verifyExistenceAndDeletion() {
        ArrayList<GetCompaniesAnswer.OnlineCustomer> companies = GetCustomers(getUser(),
                getPassword(), getAppId());
        String token = GetToken(getUser(), getPassword(), companies.get(0).companyId);

        String photoId = CreatePhoto(token, null, getSimpleMetaData());

        // Verify that it has been created
        ArrayList<OnlineMetaData> metaDataList = GetMetaDataList(token, null);
        boolean photoFound = false;
        for (OnlineMetaData photo : metaDataList) {
            if (photo.photoId.equalsIgnoreCase(photoId)) {
                photoFound = true;
                break;
            }
        }
        assertTrue("Photo has been uploaded but the meta data list does not include it", photoFound);

        DeletePhoto(token, photoId, null);

        // Verify that it has been deleted
        metaDataList = GetMetaDataList(token, null);
        photoFound = false;
        for (OnlineMetaData photo : metaDataList) {
            if (photo.photoId.equalsIgnoreCase(photoId)) {
                photoFound = true;
                break;
            }
        }
        assertFalse("Photo has been uploaded and deleted but the meta data list still includes it", photoFound);
    }

    public void testGetMetaDataList_simple() {
        ArrayList<GetCompaniesAnswer.OnlineCustomer> companies = GetCustomers(getUser(),
                getPassword(), getAppId());
        String token = GetToken(getUser(), getPassword(), companies.get(0).companyId);

        GetMetaDataList(token, null);
    }

    public void testUpdatePhoto() {
        ArrayList<GetCompaniesAnswer.OnlineCustomer> companies = GetCustomers(getUser(),
                getPassword(), getAppId());
        String token = GetToken(getUser(), getPassword(), companies.get(0).companyId);

        OnlineMetaData originalMetaData = getSimpleMetaData();
        String photoId = CreatePhoto(token, null, originalMetaData);
        OnlineMetaData updatedMetaData = getExtendedMetaData();
        updatedMetaData.photoId = photoId;
        UpdatePhoto(token, null, updatedMetaData);

        // Verify that the meta data has been updated
        ArrayList<OnlineMetaData> metaDataList = GetMetaDataList(token, null);
        boolean photoFound = false;
        for (OnlineMetaData photo : metaDataList) {
            if (photo.photoId.equalsIgnoreCase(photoId)) {
                photoFound = true;
                compareMetaData(updatedMetaData, photo);
                break;
            }
        }
        assertTrue("Photo has been uploaded but the meta data list does not include it", photoFound);

        // Clean up
        DeletePhoto(token, photoId, null);
    }

    public void testDownloadPhoto() {
        ArrayList<GetCompaniesAnswer.OnlineCustomer> companies = GetCustomers(getUser(),
                getPassword(), getAppId());
        String token = GetToken(getUser(), getPassword(), companies.get(0).companyId);

        String photoId = CreatePhoto(token, null, getSimpleMetaData());


        DownloadPhoto(token, null, photoId);

        // Clean up
        DeletePhoto(token, photoId, null);
    }

    // doGetDocument
    // doRegisterDevice

    private ArrayList<GetCompaniesAnswer.OnlineCustomer> mCompanies;

    private ArrayList<GetCompaniesAnswer.OnlineCustomer> GetCustomers(String user, String
            password, int appId) {
        final CountDownLatch signal = new CountDownLatch(1);

        GetCompaniesRequest<GetCompaniesAnswer> request = new GetCompaniesRequest<GetCompaniesAnswer>(getContext(),
                user,
                password,
                appId,
                GetCompaniesAnswer.class,
                new Response.Listener<GetCompaniesAnswer>() {
                    @Override
                    public void onResponse(final GetCompaniesAnswer response) {
                        int numberOfCompanies;
                        if(response.onlineCustomers == null){
                            numberOfCompanies = 0;
                        } else{
                            numberOfCompanies = response.onlineCustomers.size();
                        }

                        if (numberOfCompanies == 0) {
                            assertFalse("No companies associated with the user", numberOfCompanies == 0);
                        } else {

                            mCompanies = response.onlineCustomers;
                            assertNotNull("Found no Visma SPCS companies", mCompanies);
                        }

                        signal.countDown();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error instanceof BlueNetworkError) {
                            BlueNetworkError blueNetworkError = (BlueNetworkError) error;
                            assertTrue(String.format("Error when getting list of users.\nHTTPStatusCode: %1%s, ResponseCode: %2%s", blueNetworkError.blueError, blueNetworkError.blueMessage), false);
                        } else {
                            assertTrue(String.format("Error when getting list of users."), false);
                        }

                        signal.countDown();
                    }
                });

        VolleySingleton.getInstance().addToRequestQueue(request);

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return mCompanies;
    }

    private String mToken;

    private String GetToken(String user, String password, String companyId) {
        final CountDownLatch signal = new CountDownLatch(1);

        GetTokenRequest<GetTokenAnswer> request = new GetTokenRequest<GetTokenAnswer>(getContext(),
                user,
                password,
                companyId,
                GetTokenAnswer.class,
                new Response.Listener<GetTokenAnswer>() {
                    @Override
                    public void onResponse(final GetTokenAnswer response) {
                        assertFalse("Token is empty", TextUtils.isEmpty(response.token));
                        mToken = response.token;
                        signal.countDown();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        assertTrue("Error when getting token", false);
                        signal.countDown();
                    }
                });

        // Add the request to the RequestQueue.
        VolleySingleton.getInstance().addToRequestQueue(request);

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return mToken;
    }

    private String mInboundEmail;

    private String GetInboundEmail(String token) {
        final CountDownLatch signal = new CountDownLatch(1);

        GetEmailRequest<GetEmailAnswer> request = new GetEmailRequest<GetEmailAnswer>(getContext(),
                token,
                GetEmailAnswer.class,
                new Response.Listener<GetEmailAnswer>() {
                    @Override
                    public void onResponse(final GetEmailAnswer response) {
                        assertFalse("Inbound email is empty.", TextUtils.isEmpty(response.inboundEmail));
                        assertTrue("Inbound email does not contain '@'.", response.inboundEmail.contains("@"));
                        mInboundEmail = response.inboundEmail;
                        signal.countDown();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        assertTrue("Error when getting inbound email.", false);

                        signal.countDown();
                    }
                });

        VolleySingleton.getInstance().addToRequestQueue(request);

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return mInboundEmail;
    }

        private String mPhotoId;

        private String CreatePhoto(String token, String extraData, OnlineMetaData metaData) {
            Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.GREEN);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);

            final OnlinePhoto onlinePhoto = new OnlinePhoto(metaData, baos.toByteArray());

            final CountDownLatch signal = new CountDownLatch(1);

            CreatePhotoRequest<CreatePhotoAnswer> request = new CreatePhotoRequest<CreatePhotoAnswer>(getContext(),
                    token,
                    onlinePhoto,
                    CreatePhotoAnswer.class,
                    new Response.Listener<CreatePhotoAnswer>() {
                        @Override
                        public void onResponse(final CreatePhotoAnswer response) {
                            assertFalse("The created photo did not generate an id.", TextUtils.isEmpty(response.photoGuid));
                            mPhotoId = response.photoGuid;

                            signal.countDown();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            assertTrue("Error when uploading new photo.", false);

                            signal.countDown();
                        }
                    });

            VolleySingleton.getInstance().addToRequestQueue(request);

            try {
                signal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return mPhotoId;
        }

        private void DeletePhoto(String token, String photoId, String extraData) {
            final CountDownLatch signal = new CountDownLatch(1);

            DeletePhotoRequest<Base> request = new DeletePhotoRequest<Base>(getContext(),
                    token,
                    photoId,
                    Base.class,
                    new Response.Listener<Base>() {
                        @Override
                        public void onResponse(final Base response) {
                            signal.countDown();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            assertTrue("Error when deleting photo.", false);

                            signal.countDown();
                        }
                    });

            VolleySingleton.getInstance().addToRequestQueue(request);

            try {
                signal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private ArrayList<OnlineMetaData> mMetaDataList;

        private ArrayList<OnlineMetaData> GetMetaDataList(String token, String extraData) {
            final CountDownLatch signal = new CountDownLatch(1);

            GetMetadataListRequest<GetMetadataListAnswer> request = new GetMetadataListRequest<GetMetadataListAnswer>(getContext(),
                    token,
                    GetMetadataListAnswer.class,
                    new Response.Listener<GetMetadataListAnswer>() {
                        @Override
                        public void onResponse(final GetMetadataListAnswer response) {
                            mMetaDataList = response.metaDataList;

                            signal.countDown();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            assertTrue("Error when downloading the meta data list.", false);

                            signal.countDown();
                        }
                    });

            VolleySingleton.getInstance().addToRequestQueue(request);

            try {
                signal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return mMetaDataList;
        }

        private void UpdatePhoto(String token, String extraData, OnlineMetaData metaData) {
            final CountDownLatch signal = new CountDownLatch(1);

            UpdatePhotoRequest<Base> request = new UpdatePhotoRequest<Base>(getContext(),
                    token,
                    metaData,
                    Base.class,
                    new Response.Listener<Base>() {
                        @Override
                        public void onResponse(final Base response) {
                            signal.countDown();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            assertTrue("Error when updating photo.", false);

                            signal.countDown();
                        }
                    });

            VolleySingleton.getInstance().addToRequestQueue(request);

            try {
                signal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        private void DownloadPhoto(String token, String extraData, String photoId) {
            final CountDownLatch signal = new CountDownLatch(1);

            GetPhotoRequest<GetPhotoAnswer> request = new GetPhotoRequest<GetPhotoAnswer>(getContext(),
                    token,
                    photoId,
                    GetPhotoAnswer.class,
                    new Response.Listener<GetPhotoAnswer>() {
                        @Override
                        public void onResponse(final GetPhotoAnswer response) {
                            assertFalse("The generated url is not empty", response.photoUrl.isEmpty());
                            signal.countDown();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            assertTrue("Error when downloading a photo.", false);

                            signal.countDown();
                        }
                    });

            VolleySingleton.getInstance().addToRequestQueue(request);

            try {
                signal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    private void compareMetaData(OnlineMetaData lhs, OnlineMetaData rhs) {
        assertEquals(lhs.canDelete, rhs.canDelete);
        assertEquals(lhs.comment, rhs.comment);
        assertEquals(lhs.type, rhs.type);
        assertEquals(lhs.isPaid, rhs.isPaid);
        assertEquals(lhs.date, rhs.date);
        assertEquals(lhs.invoiceDate, rhs.invoiceDate);
        assertEquals(lhs.paymentDate, rhs.paymentDate);
        assertEquals(lhs.dueDate, rhs.dueDate);
        assertEquals(lhs.usingQrString, rhs.usingQrString);

        assertEquals(lhs.name, rhs.name);
        assertEquals(lhs.organisationNumber, rhs.organisationNumber);
        assertEquals(lhs.referenceNumber, rhs.referenceNumber);
        assertEquals(lhs.dueAmount, rhs.dueAmount);
        assertEquals(lhs.dueAmount, rhs.dueAmount);
        assertEquals(lhs.highVatAmount, rhs.highVatAmount);
        assertEquals(lhs.middleVatAmount, rhs.middleVatAmount);
        assertEquals(lhs.lowVatAmount, rhs.lowVatAmount);
        assertEquals(lhs.totalVatAmount, rhs.totalVatAmount);
        assertEquals(lhs.currency, rhs.currency);
    }*/
}
