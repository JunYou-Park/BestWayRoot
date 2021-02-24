package api.model;

import java.sql.Timestamp;

public class BannerVo {
	private int bannerId;
	private String bannerThumb;
	private Timestamp bannerCreateAt;
	
	public BannerVo(int bannerId, String bannerThumb, Timestamp bannerCreateAt) {
		super();
		this.bannerId = bannerId;
		this.bannerThumb = bannerThumb;
		this.bannerCreateAt = bannerCreateAt;
	}
	
	public int getBannerId() {
		return bannerId;
	}
	
	public void setBannerId(int bannerId) {
		this.bannerId = bannerId;
	}
	
	public String getBannerThumb() {
		return bannerThumb;
	}
	
	public void setBannerThumb(String bannerThumb) {
		this.bannerThumb = bannerThumb;
	}
	
	public Timestamp getBannerCreateAt() {
		return bannerCreateAt;
	}
	
	public void setBannerCreateAt(Timestamp bannerCreateAt) {
		this.bannerCreateAt = bannerCreateAt;
	}
	
}
