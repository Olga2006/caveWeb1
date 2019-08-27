package com.cave.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.cave.beans.Bouteille;
import com.cave.beans.Utilisateur;
import com.cave.dao.BouteilleDao;
import com.cave.dao.DAOException;
import com.cave.dao.DAOFactory;
import com.cave.dao.UtilisateurDao;

@WebServlet( "/suppressionBouteille" )
public class SuppressionBouteille extends HttpServlet {
    public static final String  VUE                   = "/listeBouteilles";
    public static final String  VUE_FORM              = "/WEB-INF/jsp/restreint/afficherBouteilles.jsp";
    public static final String  ACCES_CONNEXION       = "/connection";

    public static final String  PARAM_ID_BOUTEILLE    = "idBouteille";
    public static final String  PARAM_NOM_BOUTEILLE   = "nomBouteille";
    public static final String  PARAM_IMAGE_BOUTEILLE = "imageBouteille";
    public static final String  PARAM_SESSION_USER    = "sessionUtilisateur";

    public static final String  ATT_SUCCES_DEL        = "successDel";
    public static final String  ATT_ERREURS           = "erreurs";
    public static final String  ATT_BOUTEILLES        = "bouteilles";
    public static final String  CHAMP_ERREUR_DAO      = "erreurDao";
    public static final String  CHAMP_ECHEC_DEL       = "echecDel";
    public static final String  ATT_SESSION_USER      = "sessionUtilisateur";

    public static final String  CONF_DAO_FACTORY      = "daofactory";

    public static final String  AWS_BUCKET_NAME       = "caveweb";

    private BouteilleDao        bouteilleDao;
    private UtilisateurDao      utilisateurDao;

    private String              successDel;

    private Map<String, String> erreurs               = new HashMap<String, String>();

    public void init() throws ServletException {
        /* Récupération d'une instance de nos DAO Client et Commande */
        this.bouteilleDao = ( (DAOFactory) getServletContext().getAttribute( CONF_DAO_FACTORY ) ).getBouteilleDao();
        this.utilisateurDao = ( (DAOFactory) getServletContext().getAttribute( CONF_DAO_FACTORY ) ).getUtilisateurDao();
    }

    public void doPost( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Utilisateur sessionUtilisateur = (Utilisateur) session.getValue( PARAM_SESSION_USER );
        if ( sessionUtilisateur != null ) {
            String idBouteille = getValeurParametre( request, PARAM_ID_BOUTEILLE );
            String nomBouteille = getValeurParametre( request, PARAM_NOM_BOUTEILLE );
            String imageBouteille = getValeurParametre( request, PARAM_IMAGE_BOUTEILLE );

            if ( idBouteille != null ) {
                Long id = Long.parseLong( idBouteille );
                try {
                    bouteilleDao.supprimer( id );
                    if ( imageBouteille != null ) {
                        /* Create S3 Client Object */
                        AmazonS3 s3 = AmazonS3ClientBuilder
                                .standard()
                                .withRegion( Regions.EU_WEST_3 )
                                .withCredentials( new AWSStaticCredentialsProvider(
                                        new BasicAWSCredentials( "AKIA55PLC5STVO7YDXGQ",
                                                "wMGmdgRjHcMKJFqkD9cqCxo7FW9AHC4OfdOaNhkn" ) ) )
                                .build();

                        try {

                            /* Send Delete Object Request */
                            /* Delete single object 's3.png' */

                            s3.deleteObject( AWS_BUCKET_NAME, imageBouteille );

                        } catch ( AmazonServiceException e ) {
                            // The call was transmitted successfully, but Amazon
                            // S3 couldn't process
                            // it, so it returned an error response.
                            e.printStackTrace();
                        } catch ( SdkClientException e ) {
                            // Amazon S3 couldn't be contacted for a response,
                            // or the client
                            // couldn't parse the response from Amazon S3.
                            e.printStackTrace();
                        }

                        finally {

                            if ( s3 != null ) {
                                s3.shutdown();
                            }
                        }
                    }

                } catch ( DAOException e ) {
                    e.printStackTrace();
                    erreurs.put( CHAMP_ERREUR_DAO, e.getMessage() );
                    erreurs.put( CHAMP_ECHEC_DEL, nomBouteille );
                }
            }

            if ( erreurs.isEmpty() ) {
                successDel = nomBouteille;
                request.setAttribute( ATT_SUCCES_DEL, successDel );
                Long id_sessionUtilisateur = sessionUtilisateur.getId();
                Utilisateur sessionUtilisateurMAJ = utilisateurDao.trouver( id_sessionUtilisateur );
                session.setAttribute( ATT_SESSION_USER, sessionUtilisateurMAJ );
                List<Bouteille> bouteilles = sessionUtilisateurMAJ.getBouteilles();
                request.setAttribute( ATT_BOUTEILLES, bouteilles );

            } else {
                request.setAttribute( ATT_ERREURS, erreurs );

            }

            this.getServletContext().getRequestDispatcher( VUE_FORM ).forward( request, response );
        } else

            this.getServletContext().getRequestDispatcher( ACCES_CONNEXION ).forward( request, response );

    }

    /*
     * Méthode utilitaire qui retourne null si un paramètre est vide, et son
     * contenu sinon.
     */
    private static String getValeurParametre( HttpServletRequest request, String nomChamp ) {
        String valeur = request.getParameter( nomChamp );
        if ( valeur == null || valeur.trim().length() == 0 ) {
            return null;
        } else {
            return valeur;
        }
    }

}