/*
 * Promobot is licenced under the The MIT License (MIT)
 * 
 * Copyright (c) 2014–2016 Daloonik daloonik@gmail.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package promobot;

import java.io.IOException;
import java.io.LineNumberReader;

import javax.servlet.http.*;

import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

import com.sendgrid.SendGrid;
import com.sendgrid.SendGridException;

@SuppressWarnings("serial")
public class PromobotServlet extends HttpServlet
   {
   /**
    * 
    */
   public static String promo = "Your promo message";
   public static String file = "WEB-INF/StaticFiles/emails";

   DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
   Entity locationEntity;
   
   //Change this value with your own SendGrid API key
   SendGrid sendgrid = new SendGrid("your_sendgrid_api_key");
   
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
      {
      LineNumberReader reader = new LineNumberReader(new FileReader(file));

      try
         {
         String buf, username=null;
         int location = 1;  
         
         locationEntity = datastore.get(KeyFactory.createKey("locationEntity", "LE"));
         location = (int)Long.parseLong(locationEntity.getProperty("location").toString());
         
         while(true)
            {
            if((buf = reader.readLine()) == null)
               {
               //start over
               location = 1;
               locationEntity.setProperty("location", Integer.toString(location));
               datastore.put(locationEntity);
               break;
               }
            else
               {
               if(reader.getLineNumber() == location)
                  {
                  username = buf;
                  location++;
                  locationEntity.setProperty("location", Integer.toString(location));
                  datastore.put(locationEntity);
                  break;
                  }
               }
            } 
         SendGrid.Email email = new SendGrid.Email();
         email.addTo(username);
         email.setFrom("youremail@promo.com");
         email.setSubject("Promo subject");
         email.setText(promo);

         SendGrid.Response response = sendgrid.send(email);
        
         resp.setContentType("text/plain");

         resp.getWriter().println(response.getMessage());
         resp.getWriter().println("Message sent to "+username+ " : "+promo);         
         }
      catch (SendGridException e)
         {
         resp.setContentType("text/plain");
         resp.getWriter().println(e.toString());
         }    
      catch(FileNotFoundException e)
         {
         resp.setContentType("text/plain");
         resp.getWriter().println(e.toString());       
         }
      catch (EntityNotFoundException e) 
         {
         // Set to 1
         locationEntity = new Entity("locationEntity", "LE");
         locationEntity.setProperty("location", "1");
         datastore.put(locationEntity);
         }
      catch(NoSuchElementException e)
         {
         resp.setContentType("text/plain");
         resp.getWriter().println(e.toString());   
         }
      finally
         {
         if(reader != null)
            reader.close();
         }
      }
   }
