package com.ibaseit.demo;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebPageScraping {

	public static void main(String[] args) throws Exception {
		HttpClient client = HttpClientBuilder.create().build();
		// HttpClient client =
		// HttpClientBuilder.create().setRedirectStrategy(new
		// LaxRedirectStrategy()).build();

		long startTotalTime = System.currentTimeMillis();
		// Session Handling
		// CookieStore cookieStore = new BasicCookieStore();
		HttpContext httpContext = new BasicHttpContext();
		httpContext.setAttribute(HttpClientContext.COOKIE_STORE, new BasicCookieStore());

		long startTime = System.currentTimeMillis();
		// Step1 : GET Request for login page
		HttpResponse response = client.execute(new HttpGet("https://merituspayment.com/merchants/frmLogin.aspx"), httpContext);
		System.out.println("Step 1:" + (System.currentTimeMillis() - startTime));

		// Step2 : Setting Userid and password for login page
		startTime = System.currentTimeMillis();
		List<NameValuePair> postParams = setFormParamsForLogin(response, "31495", "1326C0mcastic");

		System.out.println("Step 2:" + (System.currentTimeMillis() - startTime));

		// Step3 : post request for login page
		startTime = System.currentTimeMillis();

		HttpPost post = new HttpPost("https://merituspayment.com/merchants/frmLogin.aspx");
		post.setEntity(new UrlEncodedFormEntity(postParams));
		client.execute(post, httpContext);

		System.out.println("Step 3:" + (System.currentTimeMillis() - startTime));

		// Step4 : Setting Form parameters for Charge Back xls download
		startTime = System.currentTimeMillis();
		List<NameValuePair> postParamsLink = setFormDateParams();

		System.out.println("Step 4:" + (System.currentTimeMillis() - startTime));

		// Step5 : Post Request for XLS download
		startTime = System.currentTimeMillis();
		// HttpResponse response1 = http.sendPostForExcel(postParamsLink,
		// httpContext);

		post = new HttpPost("https://merituspayment.com/merchants/web/SecureReportForms/frmChargebackDetail.aspx?ct=0&dt=0&rd=0");
		post.setEntity(new UrlEncodedFormEntity(postParamsLink));
		response = client.execute(post, httpContext);

		System.out.println("Step 5:" + (System.currentTimeMillis() - startTime));

		// Step6 : writing data into xls
		startTime = System.currentTimeMillis();
		writeExcel(response);

		System.out.println("Step 6:" + (System.currentTimeMillis() - startTime));
		long endTotalTime = System.currentTimeMillis();
		System.out.println("Total Time : " + (endTotalTime - startTotalTime));
	}

	private static void writeExcel(HttpResponse response) throws UnsupportedOperationException, IOException {

		List<PaysafeHtmlData> logDetails = new ArrayList<PaysafeHtmlData>();

		String page = getResponseString(response);

		for (Element table : Jsoup.parse(page.substring(page.split("<table")[0].length())).getElementsByTag("table")) {
			Elements trGroup = table.getElementsByTag("tr");
			// System.out.println("table : " + table + " ,  trgroup : " +
			// trGroup);
			for (int trNo = 0; trNo < trGroup.size() - 1; trNo++) {
				Element tr = trGroup.get(trNo);
				Elements noOfrows = null;

				if (trNo == 0)
					noOfrows = tr.getElementsByTag("th");
				if (trNo > 0)
					noOfrows = tr.getElementsByTag("td");

				// System.out.println("tr : " + tr + " ,  rows : " + noOfrows);
				PaysafeHtmlData paysafeLogDetails = new PaysafeHtmlData(noOfrows);
				logDetails.add(paysafeLogDetails);
			}
		}

		generateLogFile(logDetails);
	}

	public static List<NameValuePair> setFormParamsForLogin(HttpResponse page, String username, String password)
			throws UnsupportedOperationException, IOException {

		// System.out.println("Extracting form's data...");
		String html = getResponseString(page);
		Document doc = Jsoup.parse(html);

		// Form id
		Element loginform = doc.getElementById("form1");
		Elements inputElements = loginform.getElementsByTag("input");

		List<NameValuePair> paramList = new ArrayList<NameValuePair>();

		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");

			if (key.equals("ctl00$ContentPlaceHolder1$txtLoginID"))
				value = username;
			else if (key.equals("ctl00$ContentPlaceHolder1$txtPassword"))
				value = password;

			paramList.add(new BasicNameValuePair(key, value));

		}

		return paramList;
	}

	public static List<NameValuePair> setFormDateParams() {
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		paramList.add(new BasicNameValuePair("__EVENTTARGET", "ctl00$ContentPlaceHolder1$btnExpExcelChargebackDay"));
		paramList.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
		paramList.add(new BasicNameValuePair("__LASTFOCUS", ""));
		paramList
				.add(new BasicNameValuePair(
						"__VIEWSTATE",
						"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"));
		paramList.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", "78FC0077"));
		paramList.add(new BasicNameValuePair("__VIEWSTATEENCRYPTED", ""));
		paramList
				.add(new BasicNameValuePair(
						"__PREVIOUSPAGE",
						"EycxxWOUmf7xPBWRdi6KTxWkK1kjnJlu-Gv6NwWq4MLwpQWGzFfCmCnufngiSgFJC-lANh4k7hKCFYCM4IGtnUazq0drd1WBRKrqsdqtoCkIMrcmaTz8mij1hA4f8U8NxfF20-tTt1eYypZMojDw5rivtB55Fe4aAYKcOGhN3Ls1"));
		paramList.add(new BasicNameValuePair("ContentPlaceHolder1_wdcFromDate_clientState",
				"|0|012016-11-1-0-0-0-0||[[[[]],[],[]],[{},[]],\"012016-10-1-0-0-0-0\"]"));
		paramList.add(new BasicNameValuePair("ContentPlaceHolder1_wdcToDate_clientState",
				"|0|012016-11-14-0-0-0-0||[[[[]],[],[]],[{},[]],\"012016-10-31-0-0-0-0\"]"));
		paramList.add(new BasicNameValuePair("ctl00$ContentPlaceHolder1$ddlCBType", "-1"));
		paramList.add(new BasicNameValuePair("ctl00$ContentPlaceHolder1$ddlTransType", "-1"));
		paramList.add(new BasicNameValuePair("ctl00$ContentPlaceHolder1$ddlCardType", "-1"));
		paramList.add(new BasicNameValuePair("ContentPlaceHolder1_wceTransAmount_clientState", "|0|01||[[[[]],[],[]],[{},[]],\"01\"]"));
		paramList.add(new BasicNameValuePair("ContentPlaceHolder1_wceTransAmount", ""));
		paramList.add(new BasicNameValuePair("ContentPlaceHolder1_wteAuthCode_clientState", "|0|01||[[[[]],[],[]],[{},[]],\"01\"]"));
		paramList.add(new BasicNameValuePair("ContentPlaceHolder1_wteAuthCode", ""));
		paramList.add(new BasicNameValuePair("ctl00$ContentPlaceHolder1$tbxLastFour", ""));
		paramList.add(new BasicNameValuePair("ctl00$ContentPlaceHolder1$rdExport", "1"));
		paramList.add(new BasicNameValuePair("ctl00$ContentPlaceHolder1$cboPageSize", "10"));
		paramList.add(new BasicNameValuePair("_ig_def_dp_cal_clientState",
				"[[null,[],null],[{},[]],'01,2016,11']"));
		paramList
				.add(new BasicNameValuePair(
						"ctl00$_IG_CSS_LINKS_",
						"~/App_Themes/Blue/Blue.css|../../ig_res/Default/ig_monthcalendar.css|../../ig_res/Default/ig_dialogwindow.css|../../ig_res/Default/ig_texteditor.css|../../ig_res/Default/ig_shared.css"));
		return paramList;
	}

	private static void generateLogFile(List<PaysafeHtmlData> logDetails) throws IOException {

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("ChargeBackDetail");

		XSSFRow headerRow = sheet.createRow(0);
		headerRow.createCell(0).setCellValue("Input date");
		headerRow.createCell(1).setCellValue("Trans Date");
		headerRow.createCell(2).setCellValue("Case Number");
		headerRow.createCell(3).setCellValue("First Six and Last Four");
		headerRow.createCell(4).setCellValue("Amount");
		headerRow.createCell(5).setCellValue("Reason");

		for (int rowIndex = 1; rowIndex <= logDetails.size() - 1; rowIndex++) {
			XSSFRow row = sheet.createRow(rowIndex);
			PaysafeHtmlData currentObj = logDetails.get(rowIndex);
			row.createCell(0).setCellValue(currentObj.getInputDate());
			row.createCell(1).setCellValue(currentObj.getTransDate());
			row.createCell(2).setCellValue(currentObj.getCaseNumber());
			row.createCell(3).setCellValue(currentObj.getFirstSixAndLastFour());
			row.createCell(4).setCellValue(currentObj.getAmount());
			row.createCell(5).setCellValue(currentObj.getReason());
		}

		String filePath = "output\\paysafe\\" + System.currentTimeMillis() + ".xlsx";
		FileOutputStream fileOutputStream = new FileOutputStream(filePath);
		wb.write(fileOutputStream);
		fileOutputStream.close();
		wb.close();

	}

	private static String getResponseString(HttpResponse response) throws UnsupportedOperationException, IOException {

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuilder result = new StringBuilder();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		return result.toString();
	}

}
