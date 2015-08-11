/*
 * Copyright (c) 2015 Fengtan<https://github.com/fengtan/>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jenkinsci.plugins.drupal.beans;

/**
 * Drupal extension (theme/module).
 * 
 * @author Fengtan https://github.com/fengtan/
 *
 */
public class DrupalExtension {

	private String name;
	private String type;
	private String status;
	private String version;
	
	public DrupalExtension(String name, String type, String status, String version) {
		this.name = name;
		this.type = type;
		this.status = status;
		this.version = version;
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public String getStatus() {
		return status;
	}
	
	public String getVersion() {
		return version;
	}
	
}
