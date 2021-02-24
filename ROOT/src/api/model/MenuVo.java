package api.model;

import java.sql.Timestamp;

public class MenuVo {
    int menu_id;
    String menu_name;
    String menu_summary;
    int menu_price;
    String menu_thumb;
    Timestamp menu_create_at;

    public MenuVo(int menu_id, String menu_name, String menu_summary, int menu_price, String menu_thumb, Timestamp menu_create_at) {
        this.menu_id = menu_id;
        this.menu_name = menu_name;
        this.menu_summary = menu_summary;
        this.menu_price = menu_price;
        this.menu_thumb = menu_thumb;
        this.menu_create_at = menu_create_at;
    }

    public int getMenu_id() {
        return this.menu_id;
    }

    public void setMenu_id(int menu_id) {
        this.menu_id = menu_id;
    }

    public String getMenu_name() {
        return this.menu_name;
    }

    public void setMenu_name(String menu_name) {
        this.menu_name = menu_name;
    }

    public String getMenu_summary() {
        return this.menu_summary;
    }

    public void setMenu_summary(String menu_summary) {
        this.menu_summary = menu_summary;
    }

    public int getMenu_price() {
        return this.menu_price;
    }

    public void setMenu_price(int menu_price) {
        this.menu_price = menu_price;
    }

    public Timestamp getMenu_create_at() {
        return this.menu_create_at;
    }

    public void setMenu_create_at(Timestamp menu_create_at) {
        this.menu_create_at = menu_create_at;
    }

    public String getMenu_thumb() {
        return this.menu_thumb;
    }

    public void setMenu_thumb(String menu_thumb) {
        this.menu_thumb = menu_thumb;
    }

    public String toString() {
        return "MenuVo [menu_id=" + this.menu_id + ", menu_name=" + this.menu_name + ", menu_summary=" + this.menu_summary + ", menu_price=" + this.menu_price + ", menu_thumb=" + this.menu_thumb + ", menu_create_at=" + this.menu_create_at + "]";
    }
}
