package com.nv.commons.utils;

import com.fasterxml.jackson.core.JsonGenerator;

@FunctionalInterface
public interface JsonGenerateProcessor {

	void process(JsonGenerator jGenerator) throws Exception;

}