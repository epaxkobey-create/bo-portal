// Copyright (C) 1998-2001 by Jason Hunter <jhunter_AT_acm_DOT_org>.
// All rights reserved.  Use of this class is limited.
// Please see the LICENSE for more information.

package com.nv.commons.exceptions;

public class ParameterNotFoundException extends Deviation {

  	/**
	 * 
	 */
//	private static final long serialVersionUID = 9092066888799852173L;


  /**
   * Constructs a new ParameterNotFoundException with the specified
   * detail message.
   *
   * @param s the detail message
   */
  public ParameterNotFoundException(String s) {
    super(s);
  }
}
