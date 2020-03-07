package com.gleipnirymir.model;

public class Wish {

	private String cardName;
	// +* Andrew Le - players can now set which card of a rarity they are prioritizing.
	// In order to achieve this, a field is added to the Wish object.
	// This field is present in each Wish JSON object, and is read from each wishlist file.
	private boolean priority;

	public Wish(String cardName) {
		this.cardName = cardName;
	}

	public String getCardName() {
		return cardName;
	}

	public void setCardName(String cardName) {
		this.cardName = cardName;
	}

	// +* Andrew Le - if the priority property in the Wish JSON object was true, this returns true.
	public boolean isPriority() {
		return priority;
	}

	// +* Andrew Le - when reading from the wishlist file, the priority field is set to the JSON property.
	// This method is also called when adding and deleting priorities.
	public void setPriority(boolean priority) {
		this.priority = priority;
	}
}
