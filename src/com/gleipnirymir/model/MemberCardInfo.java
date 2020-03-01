package com.gleipnirymir.model;

public class MemberCardInfo {

    private String memberName;
    private String memberTag;
    private String cardName;
    private Integer cardQuantity;
    private Integer cardLevel;

    public MemberCardInfo(String memberName, String memberTag, String cardName, Integer cardQuantity, Integer cardLevel) {
        this.memberName = memberName;
        this.memberTag = memberTag;
        this.cardName = cardName;
        this.cardQuantity = cardQuantity;
        this.cardLevel = cardLevel;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberTag() {
        return memberTag;
    }

    public void setMemberTag(String memberTag) {
        this.memberTag = memberTag;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public Integer getCardQuantity() {
        return cardQuantity;
    }

    public void setCardQuantity(Integer cardQuantity) {
        this.cardQuantity = cardQuantity;
    }

    public Integer getCardLevel() {
        return cardLevel;
    }

    public void setCardLevel(Integer cardLevel) {
        this.cardLevel = cardLevel;
    }


}
