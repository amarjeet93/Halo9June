package com.codebrew.clikat.module.cart.model;

public class TiPModel {
    int tip;
    boolean isSelected;

    public int getTip() {
        return tip;
    }

    public void setTip(int tip) {
        this.tip = tip;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public TiPModel() {

    }
    public TiPModel(int tip, boolean isSelected) {
        this.tip = tip;
        this.isSelected = isSelected;
    }
}
