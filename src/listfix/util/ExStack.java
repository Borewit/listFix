/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2012 Jeremy Caron
 * 
 *  This file is part of listFix().
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import listfix.io.Constants;

/**
 *
 * @author jcaron
 */
public class ExStack
{
	/**
	 *
	 * @param aThrowable
	 * @return
	 */
	public static String toString(Throwable aThrowable)
	{
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}

	/**
	 * 
	 * @param message
	 * @param aThrowable
	 * @return
	 */
	public static String formatErrorForUser(String message, Throwable aThrowable)
	{
		return "<html><body>" + message + "<BR/><BR/>" + getHTMLDetails(aThrowable) + "</body></html>";
	}
	
	/**
	 * Return an HTML-formatted stack trace for the specified Throwable,
	 * including any exceptions chained to the exception. Note the use of the
	 * Java 1.4 StackTraceElement to get stack details. The returned string
	 * begins with "<html>" and is therefore suitable for display in Swing
	 * components such as JLabel.
	 */
	private static String getHTMLDetails(Throwable throwable)
	{
		StringBuilder b = new StringBuilder();
		// int lengthOfLastTrace = 1;  // initial value

		// Start with the specified throwable and loop through the chain of
		// causality for the throwable.
		while (throwable != null)
		{
			// Output Exception name and message, and begin a list 
			b.append(throwable.toString()).append(throwable.getMessage() == null ? "" : " - " + throwable.getMessage()).append("<br/>");
			// Get the stack trace and output each frame.  
			// Be careful not to repeat stack frames that were already reported
			// for the exception that this one caused.
			StackTraceElement[] stack = throwable.getStackTrace();
			for (int i = 0; i < stack.length; i++)
			{
				b.append("&nbsp;&nbsp;&nbsp;&nbsp;at ").append(stack[i].getClassName()).append(".").append(stack[i].getMethodName()).append("(").append(stack[i].getFileName()).append(":").append(stack[i].getLineNumber()).append(")<br/>");
			}
			// See if there is a cause for this exception
			throwable = null;
//					throwable.getCause();
//			if (throwable != null)
//			{
//				// If so, output a header
//				b.append("<i>Caused by: </i>");
//				// And remember how many frames to skip in the stack trace
//				// of the cause exception
//				lengthOfLastTrace = stack.length;
//			}
		}
		return b.toString();
	}
}
