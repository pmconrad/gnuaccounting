package appLayer.taxRelated;

public class IRSoffices {
	// source http://gemfa.bzst.bund.de/gemfai.exe?rel=nofollow
	private static final IRSoffice[] offices = {
			new IRSoffice("5281", "Aachen f. Steuerfahndung und Steuerstrafsachen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5271", "Aachen für Groß- und Konzernbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5202", "Aachen-Kreis"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5201", "Aachen-Stadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2850", "Aalen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5301", "Ahaus"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2311", "Alfeld (Leine)"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2601", "Alsfeld-Lauterbach Verwaltungsstelle Alsfeld"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2629", "Alsfeld-Lauterbach Verwaltungsstelle Lauterbach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5302", "Altena"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4166", "Altenburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2702", "Altenkirchen-Hachenburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2718", "Altenkirchen-Hachenburg Aussenstelle Hachenburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9201", "Amberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3062", "Angermünde"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3217", "Annaberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9203", "Ansbach mit Außenstellen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5303", "Arnsberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9204", "Aschaffenburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9102", "Augsburg-Land"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9103", "Augsburg-Stadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9101", "Augsburg-Stadt Arbeitnehmerbereich"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2354", "Aurich"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2851", "Backnang"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2355", "Bad Bentheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2312", "Bad Gandersheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2603", "Bad Homburg v.d. Höhe"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9205", "Bad Kissingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2706", "Bad Kreuznach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2701", "Bad Neuenahr-Ahrweiler"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9206", "Bad Neustadt a.d.S."), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2111", "Bad Segeberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2132", "Bad Segeberg Außenst. Norderstedt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9104", "Bad Tölz -Außenstelle des Finanzamts Wolfratshausen-"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2889", "Bad Urach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2833", "Baden-Baden"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2836", "Baden-Baden Außenstelle Bühl"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2853", "Balingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9207", "Bamberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3204", "Bautzen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9208", "Bayreuth"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5304", "Beckum"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2605", "Bensheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2617", "Bensheim Außenstelle Fürth"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9105", "Berchtesgaden-Laufen"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("4083", "Bergen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5203", "Bergheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5204", "Bergisch Gladbach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5174", "Bergisches Land für Groß- und  Konzernbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1113", "Berlin Charlottenburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1114", "Berlin Friedrichshain-Kreuzberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1138", "Berlin für Fahndung und Strafsachen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1127", "Berlin für Körperschaften I"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1137", "Berlin für Körperschaften II"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1129", "Berlin für Körperschaften III"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1130", "Berlin für Körperschaften IV"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1132", "Berlin Lichtenberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1133", "Berlin Marzahn-Hellersdorf"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1134", "Berlin Mitte/Tiergarten"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1116", "Berlin Neukölln"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1135", "Berlin Pankow/Weißensee"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1131", "Berlin Prenzlauer Berg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1128", "Berlin Prenzlauer Berg - nur KFZ-Steuer -"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1117", "Berlin Reinickendorf"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1118", "Berlin Schöneberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1119", "Berlin Spandau"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1120", "Berlin Steglitz"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1121", "Berlin Tempelhof"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1136", "Berlin Treptow/Köpenick"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1123", "Berlin Wedding"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1124", "Berlin Wilmersdorf"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1125", "Berlin Zehlendorf"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2743", "Bernkastel-Wittlich"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2707", "Bernkastel-Wittlich Aussenstelle Bernkastel-Kues"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2854", "Biberach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2879", "Biberach Außenstelle Riedlingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5381", "Bielefeld f. Steuerfahndung und Steuerstrafsachen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5371", "Bielefeld für Groß- und Konzernbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5349", "Bielefeld-Außenstadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5305", "Bielefeld-Innenstadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2855", "Bietigheim-Bissingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2708", "Bingen-Alzey"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2703", "Bingen-Alzey Aussenstelle Alzey"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2710", "Bitburg-Prüm"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2736", "Bitburg-Prüm Aussenstelle Prüm"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3116", "Bitterfeld-Wolfen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2856", "Böblingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5382", "Bochum f. Steuerfahndung und Steuerstrafsachen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5306", "Bochum-Mitte"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5350", "Bochum-Süd"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5282", "Bonn f. Steuerfahndung und Steuerstrafsachen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5272", "Bonn für Groß- und Konzernbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5206", "Bonn-Außenstadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5205", "Bonn-Innenstadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5307", "Borken"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3235", "Borna"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5308", "Bottrop"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3048", "Brandenburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2390", "Braunschweig für Fahndung und Strafsachen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2380", "Braunschweig für Großbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2313", "Braunschweig-Altewiekring"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2314", "Braunschweig-Wilhelmstr."), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2478", "Bremen für Außenprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2471", "Bremen-Nord"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("2474", "Bremen-Nord"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2484", "Bremen-Nord Arbeitnehmerbereic"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2457", "Bremen-Nord Bewertung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2476", "Bremen-Nord KraftfahrzeugSt"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("2472", "Bremen-Ost"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("2482", "Bremen-Ost Arbeitnehmerbereich"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("2473", "Bremen-West"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("2483", "Bremen-West Arbeitnehmerbereic"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2475", "Bremerhaven"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2485", "Bremerhaven Arbeitnehmerbereich"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2477", "Bremerhaven Bewertung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5309", "Brilon"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2830", "Bruchsal"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5224", "Brühl"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2315", "Buchholz in der Nordheide"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5310", "Bünde"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2316", "Burgdorf"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9106", "Burghausen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3057", "Calau"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2845", "Calw"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2317", "Celle"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9211", "Cham mit Außenstellen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3215", "Chemnitz-Mitte"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3214", "Chemnitz-Süd"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2356", "Cloppenburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9212", "Coburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5312", "Coesfeld"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3056", "Cottbus"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2318", "Cuxhaven"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9107", "Dachau"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2607", "Darmstadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2713", "Daun"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9108", "Deggendorf"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2357", "Delmenhorst"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3114", "Dessau-Roßlau"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5313", "Detmold"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5373", "Detmold für Groß- und Konzernbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2608", "Dieburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2609", "Dillenburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9109", "Dillingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9110", "Dingolfing"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5101", "Dinslaken"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2123", "Dithmarschen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2116", "Dithmarschen Außenstelle Meldorf"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3236", "Döbeln"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9111", "Donauwörth -Außenstelle des Finanzamts Nördlingen-"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5374", "Dortmund für Groß- und Konzernbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5315", "Dortmund-Hörde"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5317", "Dortmund-Ost"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5316", "Dortmund-Unna"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5314", "Dortmund-West"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("3201", "Dresden I"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3202", "Dresden-Nord"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3203", "Dresden-Süd"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5107", "Duisburg-Hamborn"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5109", "Duisburg-Süd"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5134", "Duisburg-West"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5207", "Düren"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5181", "Düsseldorf f. Steuerfahndung und Steuerstrafsachen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5170", "Düsseldorf I für Groß- und Konzernbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5171", "Düsseldorf II für Groß- und Konzernbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5103", "Düsseldorf-Altstadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5147", "Düsseldorf-Mettmann"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5133", "Düsseldorf-Mitte"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5105", "Düsseldorf-Nord"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5106", "Düsseldorf-Süd"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9112", "Ebersberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3065", "Eberswalde"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2112", "Eckernförde-Schleswig"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2129", "Eckernförde-Schleswig, Außenstelle Schleswig"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9113", "Eggenfelden"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2858", "Ehingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9171", "Eichstätt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3237", "Eilenburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4155", "Eisenach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3118", "Eisleben"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2113", "Elmshorn"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2358", "Emden"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2805", "Emmendingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9114", "Erding"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4151", "Erfurt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5208", "Erkelenz"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9216", "Erlangen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2610", "Eschwege-Witzenhausen Verwaltungsstelle Eschwege"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2641", "Eschwege-Witzenhausen Verwaltungsstelle Witzenhausen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5182", "Essen f. Steuerfahndung und Steuerstrafsachen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5172", "Essen für Groß- und Konzernbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("5110", "Essen-Nord"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5111", "Essen-NordOst"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5112", "Essen-Süd"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2859", "Esslingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2831", "Ettlingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5209", "Euskirchen"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("3058", "Finsterwalde"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2115", "Flensburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9217", "Forchheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2715", "Frankenthal"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3061", "Frankfurt (Oder)"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2613", "Frankfurt am Main I"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2612", "Frankfurt am Main II"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2645", "Frankfurt am Main III"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2614", "Frankfurt am Main IV"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2647", "Frankfurt/M. V-Höchst Verwaltungsstelle Frankfurt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2615", "Frankfurt/M. V-Höchst Verwaltungsstelle Höchst"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3220", "Freiberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2807", "Freiburg-Land"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2813", "Freiburg-Land Außenstelle Titisee-Neustadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2806", "Freiburg-Stadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9115", "Freising"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3206", "Freital"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2842", "Freudenstadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2616", "Friedberg (Hessen)"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2861", "Friedrichshafen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2618", "Fulda"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9117", "Fürstenfeldbruck"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3063", "Fürstenwalde"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9218", "Fürth"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9119", "Garmisch-Partenkirchen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5210", "Geilenkirchen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5113", "Geldern"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2619", "Gelnhausen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5318", "Gelsenkirchen-Nord"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5319", "Gelsenkirchen-Süd"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3103", "Genthin"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4161", "Gera"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2620", "Gießen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2319", "Gifhorn"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5320", "Gladbeck"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2863", "Göppingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2862", "Göppingen Außenstelle Geislingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3207", "Görlitz"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2321", "Goslar"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4156", "Gotha"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2320", "Göttingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2381", "Göttingen für Großbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9157", "Grafenau"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4084", "Greifswald"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5114", "Grevenbroich"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3238", "Grimma"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2621", "Groß-Gerau"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5212", "Gummersbach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9121", "Günzburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9220", "Gunzenhausen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4086", "Güstrow"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5351", "Gütersloh"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5321", "Hagen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5383", "Hagen f. Steuerfahndung und Steuerstrafsachen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5375", "Hagen für Groß- und Konzernbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4087", "Hagenow"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("3104", "Halberstadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3105", "Haldensleben"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("3111", "Halle (Saale)-Nord"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3110", "Halle (Saale)"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2230", "Hamburg f.Verkehrst.u.Grundbes"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2210", "Hamburg f.VerkSt.u.Grundbes-10"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2216", "Hamburg f.VerkSt.u.Grundbes-16"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2220", "Hamburg f.VerkSt.u.Grundbes-20"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2235", "Hamburg f.VerkSt.u.Grundbes-35"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2227", "Hamburg für Großunternehmen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2228", "Hamburg für Prüfungsdienste und Strafsachen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2201", "Hamburg Steuerkasse"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2241", "Hamburg-Altona"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2242", "Hamburg-Am Tierpark"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2243", "Hamburg-Barmbek-Uhlenhorst"), //$NON-NLS-1$ //$NON-NLS-2$
			//new IRSoffice("2215", "Hamburg-Barmbek-Uhlenhorst 15 "), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2244", "Hamburg-Bergedorf"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2245", "Hamburg-Eimsbüttel"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2246", "Hamburg-Hansa"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2247", "Hamburg-Harburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2248", "Hamburg-Mitte"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2249", "Hamburg-Nord"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2217", "Hamburg-Nord 17"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2250", "Hamburg-Oberalster"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2251", "Hamburg-Wandsbek"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2322", "Hameln"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5322", "Hamm"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2622", "Hanau"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2391", "Hannover für Fahndung und Strafsachen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2382", "Hannover für Großbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2323", "Hannover-Land I"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2342", "Hannover-Land I Außenstelle Springe"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2327", "Hannover-Land II"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2324", "Hannover-Mitte"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2325", "Hannover-Nord"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2326", "Hannover-Süd"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5323", "Hattingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2832", "Heidelberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2864", "Heidenheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2865", "Heilbronn"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2328", "Helmstedt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5324", "Herford"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5372", "Herne für Groß- und Konzernbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5325", "Herne"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("5344", "Herne-West"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9221", "Hersbruck"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2602", "Hersfeld-Rotenburg Verwaltungsstelle Bad Hersfeld"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2636", "Hersfeld-Rotenburg Verwaltungsstelle Rotenburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2329", "Herzberg am Harz"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5135", "Hilden"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2330", "Hildesheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9222", "Hilpoltstein"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9223", "Hof mit Außenstellen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2646", "Hofheim am Taunus"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3221", "Hohenstein-Ernstthal"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2331", "Holzminden"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1075", "Homburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1085", "Homburg - Außenst. St. Ingbert"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5326", "Höxter"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3213", "Hoyerswerda"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5327", "Ibbenbüren"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2709", "Idar-Oberstein"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4154", "Ilmenau"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9123", "Immenstadt -Außenstelle des Finanzamts Kempten-"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9124", "Ingolstadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5328", "Iserlohn"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2118", "Itzehoe"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4162", "Jena"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5213", "Jülich"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2719", "Kaiserslautern"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2834", "Karlsruhe-Durlach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2835", "Karlsruhe-Stadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2625", "Kassel I"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2623", "Kassel II-Hofgeismar Verwaltungsstelle Hofgeismar"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2626", "Kassel II-Hofgeismar Verwaltungsstelle Kassel"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9125", "Kaufbeuren m. ASt Füssen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9126", "Kelheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5115", "Kempen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9127", "Kempten (Allgäu)"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2119", "Kiel-Nord"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2120", "Kiel-Süd"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9227", "Kitzingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5116", "Kleve"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2722", "Koblenz"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5283", "Köln f. Steuerfahndung und Steuerstrafsachen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5270", "Köln für Groß- und Konzernbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5214", "Köln-Altstadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5215", "Köln-Mitte"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5217", "Köln-Nord"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5218", "Köln-Ost"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5216", "Köln-Porz"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5219", "Köln-Süd"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5223", "Köln-West"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3049", "Königs Wusterhausen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2809", "Konstanz"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2611", "Korbach-Frankenberg Verwaltungsstelle Frankenberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2627", "Korbach-Frankenberg Verwaltungsstelle Korbach"), //$NON-NLS-1$ //$NON-NLS-2$
			//new IRSoffice("3116", "Köthen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5117", "Krefeld"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5173", "Krefeld für Groß- und Konzernbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9228", "Kronach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9229", "Kulmbach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2723", "Kusel-Landstuhl"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2725", "Kusel-Landstuhl Aussenstelle Landstuhl"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3052", "Kyritz"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2810", "Lahr"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2724", "Landau"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9131", "Landsberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9132", "Landshut"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2628", "Langen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2360", "Leer (Ostfriesland)"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3232", "Leipzig I"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3231", "Leipzig II"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5329", "Lemgo"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2870", "Leonberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5230", "Leverkusen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9230", "Lichtenfels"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2630", "Limburg-Weilburg Verwaltungsstelle Limburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2638", "Limburg-Weilburg Verwaltungsstelle Weilburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9134", "Lindau"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2361", "Lingen (Ems)"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5330", "Lippstadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3208", "Löbau"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9231", "Lohr a. Main mit Außenstellen "), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2811", "Lörrach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5331", "Lübbecke"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2122", "Lübeck"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2332", "Lüchow"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3050", "Luckenwalde"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5332", "Lüdenscheid"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5333", "Lüdinghausen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2871", "Ludwigsburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2727", "Ludwigshafen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2333", "Lüneburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2392", "Lüneburg für Fahndung und Strafsachen"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("3101", "Magdeburg I"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3102", "Magdeburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2726", "Mainz-Mitte"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2728", "Mainz-Süd"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4071", "Malchin"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2837", "Mannheim-Neckarstadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2838", "Mannheim-Stadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2606", "Marburg-Biedenkopf Verwaltungsstelle Biedenkopf"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2631", "Marburg-Biedenkopf Verwaltungsstelle Marburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5359", "Marl"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2729", "Mayen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3209", "Meißen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9138", "Memmingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3112", "Merseburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1020", "Merzig"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5334", "Meschede"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2633", "Michelstadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9139", "Miesbach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9140", "Mindelheim -Außenstelle des Finanzamts Memmingen-"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5335", "Minden"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3222", "Mittweida"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5119", "Moers"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5121", "Mönchengladbach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5176", "Mönchengladbach für Groß- und  Konzernbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("5127", "Mönchengladbach-Rheydt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2730", "Montabaur-Diez"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2714", "Montabaur-Diez Aussenstelle Diez"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2840", "Mosbach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2846", "Mosbach Außenstelle Walld�rn"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2848", "Mühlacker"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9141", "Mühldorf"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4157", "Mühlhausen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5120", "Mülheim an der Ruhr"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2812", "Müllheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9143", "München-Abt. Körperschaften"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9187", "München-Abt. Körperschaften"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("9180", "München f. Körpersch."), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9142", "München f. Körpersch. Bewertung des Grundbesitzes"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9188", "München-Abt.Betriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9149", "München-Abteilung Erhebung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9189", "München-Abteilung Erhebung Kraftfahrzeugsteuer"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9144", "München I"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9181", "München I Arbeitnehmerbereich"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9145", "München II / III"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9147", "München II / III"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9182", "München II / III Arbeitnehmerbereich"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9183", "München II / III Arbeitnehmerbereich"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9146", "München IV / V"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9148", "München IV / V"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9184", "München IV / V Arbeitnehmerbereich"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9185", "München IV / V Arbeitnehmerbereich"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5384", "Münster f. Steuerfahndung und Steuerstrafsachen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5376", "Münster für Groß- und Konzernbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5336", "Münster-Außenstadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5337", "Münster-Innenstadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3051", "Nauen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3119", "Naumburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9151", "Neu-Ulm"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4072", "Neubrandenburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4069", "Neubrandenburg - RAB"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4070", "Neubrandenburg - RIA (Rentenempfänger im Ausland)"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9235", "Neumarkt i.d.Opf."), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2124", "Neumünster"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1030", "Neunkirchen"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("5125", "Neuss I"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5122", "Neuss"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2731", "Neustadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2732", "Neuwied"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2634", "Nidda"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2334", "Nienburg/Weser"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2362", "Norden"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2363", "Nordenham"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2121", "Nordfriesland"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2117", "Nordfriesland Außenstelle Husum"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9152", "Nördlingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2335", "Northeim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9238", "Nürnberg-Nord"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9240", "Nürnberg-Süd"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9241", "Nürnberg-Zentral"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2874", "Nürtingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2869", "Nürtingen Außenstelle Kirchheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5123", "Oberhausen-Nord"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5124", "Oberhausen-Süd"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9202", "Obernburg a. Main mit Außenstelle Amorbach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2635", "Offenbach am Main I"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2644", "Offenbach am Main II"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2814", "Offenburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2801", "Offenburg Außenstelle Achern"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2808", "Offenburg Außenstelle Kehl"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2823", "Offenburg Außenstelle Wolfach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2876", "Öhringen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2364", "Oldenburg (Oldenburg)"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2393", "Oldenburg für Fahndung und Strafsachen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2385", "Oldenburg für Großbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5338", "Olpe"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3053", "Oranienburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3239", "Oschatz"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2386", "Osnabrück für Großbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2365", "Osnabrück-Land"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2366", "Osnabrück-Stadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2336", "Osterholz-Scharmbeck"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2125", "Ostholstein"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5339", "Paderborn"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2353", "Papenburg"), //$NON-NLS-1$ //$NON-NLS-2$
			// new IRSoffice("4074", "Pasewalk"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9153", "Passau mit Außenstellen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2338", "Peine"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9154", "Pfaffenhofen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2841", "Pforzheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2849", "Pforzheim Außenstelle Neuenbürg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2131", "Pinneberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2735", "Pirmasens"), //$NON-NLS-1$ //$NON-NLS-2$
			//new IRSoffice("2746", "Pirmasens-Zweibrücken Aussenstelle Zweibrücken"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3210", "Pirna"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3223", "Plauen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2126", "Plön"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4165", "Pößneck"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3046", "Potsdam"), //$NON-NLS-1$ //$NON-NLS-2$
			//new IRSoffice("3054", "Pritzwalk"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2367", "Quakenbrück"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3117", "Quedlinburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2839", "Rastatt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2127", "Ratzeburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2877", "Ravensburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5149", "Rechenzentrum d. FinVew NRW"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5340", "Recklinghausen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9244", "Regensburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5126", "Remscheid"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2128", "Rendsburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2878", "Reutlingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2604", "Rheingau-Taunus Verwaltungsst. Bad Schwalbach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2637", "Rheingau-Taunus Verwaltungsstelle Rüdesheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4081", "Ribnitz-Damgarten"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9156", "Rosenheim m. ASt Wasserburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4079", "Rostock"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2340", "Rotenburg (Wümme)"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2819", "Rottweil"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2815", "Rottweil Außenstelle Oberndorf"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1040", "Saarbrücken Am Stadtgraben"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1070", "Saarbrücken Am Stadtgraben - Außenst. Sulzbach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1090", "Saarbrücken Am Stadtgraben - Außenst. Völklingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1055", "Saarbrücken MainzerStr"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1010", "Saarlouis"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3106", "Salzwedel"), //$NON-NLS-1$ //$NON-NLS-2$
			//new IRSoffice("3121", "Sangerhausen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5222", "Sankt Augustin"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2739", "Sankt Goarshausen-Sankt Goar"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2738", "Sankt Goarshausen-Sankt Goar Aussenstelle Sankt Goar"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5211", "Schleiden"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2882", "Schorndorf"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9159", "Schrobenhausen m. ASt Neuburg "), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9247", "Schwabach"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2883", "Schwäbisch Gmünd"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2884", "Schwäbisch Hall"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2857", "Schwäbisch Hall Außenstelle Crailsheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2624", "Schwalm-Eder Verwaltungsstelle Fritzlar"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2632", "Schwalm-Eder Verwaltungsstelle Melsungen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2642", "Schwalm-Eder Verwaltungsstelle Schwalmstadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9248", "Schwandorf mit Außenstelle Neunburg v. W."), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3218", "Schwarzenberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9249", "Schweinfurt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5341", "Schwelm"), //$NON-NLS-1$ //$NON-NLS-2$
			//new IRSoffice("4089", "Schwerin"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4090", "Schwerin"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2843", "Schwetzingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5220", "Siegburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5342", "Siegen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2885", "Sigmaringen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2881", "Sigmaringen Außenstelle Bad Saulgau"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2740", "Simmern-Zell"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2745", "Simmern-Zell Aussenstelle Zell"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2818", "Singen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2844", "Sinsheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5343", "Soest"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5128", "Solingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5129", "Solingen-West (neu: Solingen)"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2341", "Soltau"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4159", "Sondershausen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4170", "Sonneberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2741", "Speyer-Germersheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2716", "Speyer-Germersheim Aussenstelle Germersheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("1060", "St. Wendel"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2343", "Stade"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2384", "Stade für Großbetriebsprüfung"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2344", "Stadthagen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9161", "Starnberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3107", "Staßfurt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5311", "Steinfurt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3108", "Stendal"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3224", "Stollberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2130", "Stormarn"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4082", "Stralsund"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9162", "Straubing"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3064", "Strausberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2893", "Stuttgart I"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2895", "Stuttgart II"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2897", "Stuttgart III"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2892", "Stuttgart IV"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2896", "Stuttgart Zentrales Konzernprüfungsamt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2899", "Stuttgart-Körpersch."), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4171", "Suhl"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2345", "Sulingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2346", "Syke"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2880", "Tauberbischofsheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2852", "Tauberbischofsheim Außenstelle Bad Mergentheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9163", "Traunstein"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2742", "Trier"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2886", "Tübingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2821", "Tuttlingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2887", "Überlingen (Bodensee)"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2347", "Uelzen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9252", "Uffenheim"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2888", "Ulm"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2368", "Vechta"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5139", "Velbert"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2348", "Verden (Aller)"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5102", "Viersen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2822", "Villingen-Schwenningen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2804", "Villingen-Schwenningen Außenstelle Donaueschingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2890", "Waiblingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9254", "Waldsassen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2820", "Waldshut-Tiengen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2816", "Waldshut-Tiengen Außenstelle Bad Säckingen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2891", "Wangen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5345", "Warburg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4075", "Waren"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5346", "Warendorf"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9255", "Weiden i.d.Opf."), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9168", "Weilheim-Schongau"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2847", "Weinheim"), //$NON-NLS-1$ //$NON-NLS-2$
			//new IRSoffice("3109", "Wernigerode"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5130", "Wesel"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2349", "Wesermünde"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2369", "Westerstede"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2639", "Wetzlar"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5347", "Wiedenbrück"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2640", "Wiesbaden I"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2643", "Wiesbaden II"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2370", "Wilhelmshaven"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2350", "Winsen (Luhe)"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5221", "Wipperfürth"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4080", "Wismar"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5348", "Witten"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3115", "Wittenberg"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2371", "Wittmund"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2351", "Wolfenbüttel"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9169", "Wolfratshausen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("4085", "Wolgast"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2744", "Worms-Kirchheimbolanden"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2721", "Worms-Kirchheimbolanden Aussenstelle Kirchheimbolanden"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9258", "Wunsiedel mit Außenstelle Selb"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5183", "Wuppertal f. Steuerfahndung und Steuerstrafsachen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5131", "Wuppertal-Barmen"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("5132", "Wuppertal-Elberfeld"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9257", "Würzburg mit Außenstelle Ochsenfurt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9259", "Zeil am Main mit Außenstelle Ebern"), //$NON-NLS-1$ //$NON-NLS-2$
			//new IRSoffice("3120", "Zeitz"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("2352", "Zeven"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3228", "Zschopau"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("3227", "Zwickau"), //$NON-NLS-1$ //$NON-NLS-2$
			//new IRSoffice("3226", "Zwickau-Stadt"), //$NON-NLS-1$ //$NON-NLS-2$
			new IRSoffice("9170", "Zwiesel m. ASt Viechtach") }; //$NON-NLS-1$ //$NON-NLS-2$

	public static IRSoffice[] getIRSOffices() {
		return offices;
	}
}
