var articlesElt = document.getElementById("articles");
ajaxGet("https://private-anon-2a5c463736-globalwinescore.apiary-mock.com/globalwinescores/latest/", function (reponse) {
    // Transforme la réponse en un tableau d'articles
    var lien = JSON.parse(reponse);
   
        // Ajout du titre et du contenu de chaque article
        var titreElt = document.createElement("h2");
        titreElt.textContent = lien.results[0].appellation;
/*        var urlElt = document.createElement("p");
        urlElt.textContent = lien.vintage;
        var auteurElt = document.createElement("p");
        auteurElt.textContent = lien.score;*/
        
        articlesElt.appendChild(titreElt);
/*        articlesElt.appendChild(urlElt);
        articlesElt.appendChild(auteurElt);*/

});












// Exécute un appel AJAX GET
// Prend en paramètres l'URL cible et la fonction callback appelée en cas de succès
function ajaxGet(url, callback) {
    var req = new XMLHttpRequest();
    req.open("GET", url);
    req.addEventListener("load", function () {
        if (req.status >= 200 && req.status < 400) {
            // Appelle la fonction callback en lui passant la réponse de la requête
            callback(req.responseText);
        } else {
            console.error(req.status + " " + req.statusText + " " + url);
        }
    });
    req.addEventListener("error", function () {
        console.error("Erreur réseau avec l'URL " + url);
    });
    req.send(null);
}

//Exécute un appel AJAX POST
//Prend en paramètres l'URL cible, la donnée à envoyer et la fonction callback appelée en cas de succès
//Le paramètre isJson permet d'indiquer si l'envoi concerne des données JSON
function ajaxPost(url, data, callback, isJson) {
 var req = new XMLHttpRequest();
 req.open("POST", url);
 req.addEventListener("load", function () {
     if (req.status >= 200 && req.status < 400) {
         // Appelle la fonction callback en lui passant la réponse de la requête
         callback(req.responseText);
     } else {
         console.error(req.status + " " + req.statusText + " " + url);
     }
 });
 req.addEventListener("error", function () {
     console.error("Erreur réseau avec l'URL " + url);
 });
 if (isJson) {
     // Définit le contenu de la requête comme étant du JSON
     req.setRequestHeader("Content-Type", "application/json");
     // Transforme la donnée du format JSON vers le format texte avant l'envoi
     data = JSON.stringify(data);
 }
 req.send(data);
}