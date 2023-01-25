package com.golfingbuddy.ui.mailbox.compose;

/**
 * Created by kairat on 3/20/15.
 */
public interface RecipientInterface {
    String getOpponentId();
    String getDisplayName();
    String getAvatarUrl();

    void setOpponentId(String opponentId);
    void setDisplayName(String displayName);
    void setAvatarUrl(String avatarUrl);
}
