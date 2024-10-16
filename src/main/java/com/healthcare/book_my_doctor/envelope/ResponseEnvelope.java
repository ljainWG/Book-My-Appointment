package com.healthcare.book_my_doctor.envelope;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.book_my_doctor.enums.ResponseStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseEnvelope {

	@JsonProperty("status")
	private ResponseStatus status;

	@JsonProperty("message")
	private String message;

	@JsonProperty("data")
	private Object data;

	@JsonProperty("error")
	private Object error;

	@JsonProperty("current_page_no")
	private Integer currentPageNo;

	@JsonProperty("total_no_of_records")
	private Integer totalNoOfRecords;

	@JsonProperty("total_no_of_pages")
	private Integer totalNoOfPages;

	@JsonProperty("records_per_page")
	private Integer recordsPerPage;

	@JsonProperty("time_stamp")
	private LocalDateTime timeStamp;
}
