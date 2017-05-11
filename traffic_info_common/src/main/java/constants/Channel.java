package constants;

public enum Channel {

	Display(1, "Display"),
	WebMobile(2, "Web Mobile"),
	MobileApplication(3, "Mobile Application"),
	Video(4, "Video"),
	MobileWebVideo(5, "Mobile Web Video");

	private final Integer value;
    private final String discription;
    
    private Channel(Integer value, String discription) {
    	this.value = value;
    	this.discription = discription;
    }

	public Integer getValue() {
		return value;
	}

	public String getDiscription() {
		return discription;
	}
	
	public static Channel getChannel(String name) {
		Channel[] channelArr = values();
		
		for (Channel channel : channelArr) {
			if (channel.name().equals(name)) {
				return channel;
			}
		}
		
		return null;
	}
	
	public static Channel getChannel(Integer value) {
		Channel[] channelArr = values();
		
		for (Channel channel : channelArr) {
			if (channel.getValue().equals(value)) {
				return channel;
			}
		}
		
		return null;
	}
};
