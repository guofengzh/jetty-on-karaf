package org.web.sample;

import java.security.Principal;

import javax.security.auth.Subject;
import java.security.acl.Group ;

import org.eclipse.jetty.http.security.Credential;
import org.eclipse.jetty.security.MappedLoginService.KnownUser;
import org.eclipse.jetty.security.MappedLoginService.RolePrincipal;
import org.eclipse.jetty.server.UserIdentity;

import org.eclipse.jetty.security.DefaultUserIdentity ;
import org.eclipse.jetty.security.DefaultIdentityService ;

import java.util.ArrayList ;
import java.util.Enumeration ;

public class IdentityService4KarafJAAS extends DefaultIdentityService
{
    public UserIdentity newUserIdentity(final Subject subject, final Principal userPrincipal, final String[] roles)
    {
		ArrayList<String> roleArray = new ArrayList<String>() ;
        for (Principal p : subject.getPrincipals()) {
           if ((p instanceof Group) && ("ROLES".equalsIgnoreCase(p.getName()))) {
                Group g = (Group) p;
                Enumeration<? extends Principal> members = g.members();
                while (members.hasMoreElements()) {
                    Principal member = members.nextElement();
                    roleArray.add(member.getName());
		        }
			}
		}
        return new DefaultUserIdentity(subject,userPrincipal,roleArray.toArray( new String[roleArray.size()] ) );
    }

}
