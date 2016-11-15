package com.ibaseit.demo;
import org.jsoup.select.Elements;

public class PaysafeHtmlData {
	private String inputDate;
	private String transDate;
	private String caseNumber;
	private String firstSixAndLastFour;
	private String amount;
	private String reason;

	public PaysafeHtmlData(Elements noOfrows) {
		inputDate = noOfrows.get(0).text();
		transDate = noOfrows.get(1).text();
		caseNumber = noOfrows.get(2).text();
		firstSixAndLastFour = noOfrows.get(6).text();
		amount = noOfrows.get(7).text();
		reason = noOfrows.get(8).text();

	}

	public String getInputDate() {
		return inputDate;
	}

	public String getTransDate() {
		return transDate;
	}

	public String getCaseNumber() {
		return caseNumber;
	}

	public String getFirstSixAndLastFour() {
		return firstSixAndLastFour;
	}

	public String getAmount() {
		return amount;
	}

	public String getReason() {
		return reason;
	}

}