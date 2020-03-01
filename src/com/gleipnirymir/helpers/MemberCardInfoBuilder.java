package com.gleipnirymir.helpers;

import com.gleipnirymir.model.MemberCardInfo;

public class MemberCardInfoBuilder {
    private String memberName;
    private String memberTag;
    private String cardName;
    private Integer cardQuantity;
    private Integer cardLevel;

    public MemberCardInfoBuilder setMemberName(String memberName) {
        this.memberName = memberName;
        return this;
    }

    public MemberCardInfoBuilder setMemberTag(String memberTag) {
        this.memberTag = memberTag;
        return this;
    }

    public MemberCardInfoBuilder setCardName(String cardName) {
        this.cardName = cardName;
        return this;
    }

    public MemberCardInfoBuilder setCardQuantity(Integer cardQuantity) {
        this.cardQuantity = cardQuantity;
        return this;
    }

    public MemberCardInfoBuilder setCardLevel(Integer cardLevel) {
        this.cardLevel = cardLevel;
        return this;
    }

    public MemberCardInfo build() {
        return new MemberCardInfo(memberName, memberTag, cardName, cardQuantity, cardLevel);
    }
}