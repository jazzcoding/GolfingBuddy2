package com.golfingbuddy.model.base.classes;

/**
 * Created by sardar on 1/16/15.
 */
public class SkMenuItem {

    public enum ITEM_KEY{
        USER {
            @Override
            public String toString() {
                return "user";
            }
        },
        SEARCH {
            @Override
            public String toString() {
                return "search";
            }
        },
        GUESTS {
            @Override
            public String toString() {
                return "guests";
            }
        },
        BOOKMARKS {
            @Override
            public String toString() {
                return "bookmarks";
            }
        },
        ABOUT {
            @Override
            public String toString() {
                return "about";
            }
        },
        MATCHES {
            @Override
            public String toString() {
                return "matches";
            }
        },
        SPEED_MATCH {
            @Override
            public String toString() {
                return "speed_match";
            }
        },
        TERMS {
            @Override
            public String toString() {
                return "terms";
            }
        },
        LOGOUT {
            @Override
            public String toString() {
                return "logout";
            }
        },

        MEMBERSHIPS_AND_CREDITS {
            @Override
            public String toString() {
                return "memberships_and_credits";
            }
        },

        MEMBERSHIPS {
            @Override
            public String toString() {
                return "memberships";
            }
        },

        CREDITS {
            @Override
            public String toString() {
                return "credits";
            }
        },

        SUBSCRIBE {
            @Override
            public String toString() {
                return "subscribe";
            }
        },
        HOTLIST {
            @Override
            public String toString() {
                return "hot_list";
            }
        },
        MAILBOX {
            @Override
            public String toString() {
                return "mailbox";
            }
        };

        public static ITEM_KEY fromString(String text) {
            if (text != null) {
                for (ITEM_KEY b : ITEM_KEY.values()) {
                    if (text.equalsIgnoreCase(b.toString())) {
                        return b;
                    }
                }
            }
            return null;
        }
    }

    private int type;
    private String key;
    private int count;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
