package com.golfingbuddy.core;


import com.google.gson.JsonObject;

import java.util.Map;

import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.FormUrlEncodedTypedOutput;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Created by sardar on 6/2/14.
 */
public interface SkRestInterface {
    @FormUrlEncoded
    @POST("/user/authenticate")
    JsonObject authenticate( @FieldMap Map<String, String> options );

    @GET("/base/check-api")
    JsonObject checkApi();

    @Multipart
    @POST("/photo/upload")
    JsonObject uploadPhoto( @Part("file") TypedFile file, @Part("albumId") TypedString albumId);

    @FormUrlEncoded
    @POST("/site/get-info")
    JsonObject siteInfo(@FieldMap Map<String, String> options);

    @GET("/site/user-get-info")
    JsonObject userSiteInfo();

    @FormUrlEncoded
    @POST("/matches/list")
    JsonObject getMatchesList( @FieldMap Map<String, String> options );

    @FormUrlEncoded
    @POST("/speedmatches/list")
    JsonObject getSpeedmatchesList( @FieldMap Map<String, String> options );

    @FormUrlEncoded
    @POST("/speedmatches/like")
    JsonObject likeUser( @FieldMap Map<String, String> options );

    @FormUrlEncoded
    @POST("/speedmatches/skip")
    JsonObject skipUser( @FieldMap Map<String, String> options );

    @FormUrlEncoded
    @POST("/fbconnect/questions")
    JsonObject getQuestions( @FieldMap Map<String, String> options );

    @FormUrlEncoded
    @POST("/fbconnect/save")
    JsonObject saveFacebookUser( @FieldMap Map<String, String> options );

    @FormUrlEncoded
    @POST("/fbconnect/try-login")
    JsonObject tryLogin( @FieldMap Map<String, String> options );

    @FormUrlEncoded
    @POST("/bookmarks/mark")
    JsonObject bookmark( @FieldMap Map<String, String> options );
    
    @GET("/guests/userList")
    JsonObject getGuestsList();

    @GET("/bookmarks/userList")
    JsonObject getBookmarksList();

    @GET("/user/get-search-questions")
    JsonObject getSearchQuestions();

    @GET("/photo/user-photo-list/{userId}")
    JsonObject getUserPhotoList(@Path("userId") int userId);

    @GET("/photo/album-photo-list/{albumId}")
    JsonObject getAlbumPhotoList(@Path("albumId") int albumId);

    @FormUrlEncoded
    @POST("/base/authorization-action-status/")
    JsonObject getAuthorizationActionStatus(@FieldMap Map<String, String> options);

    @GET("/photo/user-album-list/{userId}")
    JsonObject getUserAlbumList(@Path("userId") int userId);

    @GET("/user/get-questions/{userId}")
    JsonObject getUserDetailsInfo(@Path("userId") int userId);

    @FormUrlEncoded
    @POST("/search/user-list")
    JsonObject getUserList(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/photo/delete-photos")
    JsonObject deletePhotoList( @FieldMap Map<String, String> options );

    @FormUrlEncoded
    @POST("/user/saveQuestion")
    JsonObject saveQuestion(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/user/sendReport")
    JsonObject sendReport(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/user/block")
    JsonObject blockUser(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/winks/send-wink")
    JsonObject sendWink(@FieldMap Map<String, String> options);

    @GET("/user/signout")
    JsonObject logout();

    @FormUrlEncoded
    @POST("/base/get-text")
    JsonObject getText( @FieldMap Map<String, String> options );

    @FormUrlEncoded
    @POST("/base/get-custom-page")
    JsonObject getCustomPage( @FieldMap Map<String, String> options );

    @GET("/billing/subscribeData/")
    JsonObject getSubscribeData();

    @FormUrlEncoded
    @POST("/billing/payment-options")
    JsonObject getPaymentOptions(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/billing/verifySale")
    JsonObject verifySale(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/billing/preverifySale")
    JsonObject preverifySale(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/billing/setTrialPlan")
    JsonObject subscribeToTrialPlan(@FieldMap Map<String, String> options);

    @GET("/hotlist/count")
    JsonObject getHotCount();

    @GET("/hotlist/list")
    JsonObject getHotList();

    @GET("/hotlist/list/add")
    JsonObject addToHotList();

    @GET("/hotlist/list/remove")
    JsonObject removeFromHotList();

    @GET("/mailbox/mode/get")
    JsonObject getMailboxModes();

    @GET("/mailbox/conversation/list/{offset}")
    JsonObject getConversationList(@Path("offset") int offset);

    @FormUrlEncoded
    @POST("/mailbox/messages")
    JsonObject getConversationMessages(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/mailbox/messages/history")
    JsonObject getConversationHistory(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/mailbox/message/send")
    JsonObject sendMessage(@FieldMap Map<String, String> options);

    @Multipart
    @POST("/mailbox/message/send-attachment")
    JsonObject sendAttachment(@Part("file") TypedFile file, @Part("opponentId") TypedString opponentId, @Part("lastMessageTimestamp") TypedString lastMessageTimestamp);

    @Multipart
    @POST("/mailbox/compose/attach-attachment")
    JsonObject attachAttachment(@Part("attach") TypedFile file, @Part("uid") TypedString uid);

    @FormUrlEncoded
    @POST("/mailbox/compose/delete-attachment")
    JsonObject deleteAttachment(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/mailbox/compose/find-user")
    JsonObject findUser(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/mailbox/compose/send")
    JsonObject sendCompose(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/mailbox/reply/send")
    JsonObject sendReply(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/mailbox/conversation/as-read")
    JsonObject conversationAsRead(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/mailbox/conversation/un-read")
    JsonObject conversationUnRead(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/mailbox/conversation/delete")
    JsonObject deleteConversation(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/mailbox/recipient/info")
    JsonObject getRecipientInfo(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/mailbox/chat-recipient/info")
    JsonObject getChatRecipientInfo(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/mailbox/authorize/info")
    JsonObject getActionInfo(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/mailbox/authorize")
    JsonObject authorizeReadMessage(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/mailbox/wink-back")
    JsonObject winkBack(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/user/mark-approval")
    JsonObject userMarkApproval(@FieldMap Map<String, String> options);

    @Multipart
    @POST("/user/avatar-change")
    JsonObject userAvatarChange(@Part("avatar") TypedFile avatar, @Part("userId") int userId);

    @FormUrlEncoded
    @POST("/sign-up/questions")
    JsonObject getSignUpQuestions(@FieldMap Map<String, String> options);

    @Multipart
    @POST("/sign-up/upload-tmp-avatar")
    JsonObject uploadTmpAvatar(@Part("avatar") TypedFile avatar);

    @FormUrlEncoded
    @POST("/questions/validate")
    JsonObject validateQuestions(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/sign-up/save")
    JsonObject joinUser( @FieldMap Map<String, String> options );

    @FormUrlEncoded
    @POST("/user/add-device-id")
    JsonObject addUserDeviceId(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/winks/accept-wink")
    JsonObject acceptWink(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/winks/ignore-wink")
    JsonObject ignoreWink(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/verify-email")
    JsonObject verifyEmail(@FieldMap Map<String, String> options);

    @FormUrlEncoded
    @POST("/resend-verification-email")
    JsonObject resendVerifyEmail(@FieldMap Map<String, String> options);
}



