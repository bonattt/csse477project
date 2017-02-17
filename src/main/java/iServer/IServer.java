/*
 * IServer.java
 * Feb 1, 2017
 *
 * Copyright (C) 2015 Chandan Raj Rupakheti
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 * 
 */
 
package iServer;

/**
 * 
 * @author Thomas Bonatti (bonattt@rose-hulman.edu)
 */
public interface IServer extends Runnable {
	public int getPort();
	public String getRootDirectory();
	public void stop();
	public boolean isStoped();
	public IServlet getServlet(String uri);
	public boolean addServlet(String context, Class<? extends IServlet> class1);
}
