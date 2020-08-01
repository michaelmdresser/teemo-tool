(ns teemo-tool.core
  (:gen-class))

(require '[irclj.core :as irc]
         '[clojure.java.io :as io]
         '[clojure.java.jdbc :as sql]
         '[clojure.java.shell :as shell])

(defn remove-db
  []
  (shell/sh "bash" "-c" "rm db/database.db"))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/database.db"})

(defn create-db-table
  []
  (sql/db-do-commands db
                      (sql/create-table-ddl :bets
                                            [[:bettor :text]
                                             [:amount :integer]
                                             [:team :text]
                                             [:timestamp :datetime :default :current_timestamp]]
                                            {:conditional? true})))

(defn get-line
  [filename]
  (with-open [rdr (io/reader filename)]
    (first (line-seq rdr))))

; the twitch username for the associated token
(defn get-username
  []
  (get-line "resources/username.txt"))

; a twitch oauth token for the associated username
; needs appropriate scopes for the twitch IRC API
(defn get-token
  []
  (get-line "resources/token.txt"))

(defn message-from-type
  [type]
  (get type :text))

(defn author-from-type
  [type]
  (get type :user))

(defn author-is-official-bot?
  [author]
  (= author "xxsaltbotxx"))

(defn bet-message?
  [text]
  (clojure.string/includes? text "Bet complete")
  )

(defn bettor-from-bet-message
  [text]
  (def bettor-reg #"@(\S+).*")
  (get (re-find bettor-reg text) 1))

; (bettor-from-bet-message blue-bet-message)

; @Heriophant - Bet complete for RED, 500. Your new balance is 397.

(defn map-from-bet
  [type]
  (def team-reg #"(?:complete for )(\S+),")
  (def bet-reg #"(?:complete for.*, )(\d+)")
  (def bet-message (message-from-type type))
  (let [team (get (re-find team-reg bet-message) 1)
        bet-amount (get (re-find bet-reg bet-message) 1)]
    {:team (clojure.string/lower-case team)
     :amount bet-amount
     :bettor (bettor-from-bet-message bet-message)}))

(defn insert-bet-map-to-db
  [db bet-map]
  (sql/insert! db :bets bet-map))

(defn privmsg-callback
  [irc type & s]
  (if (author-is-official-bot? (author-from-type type))
    (let [message (message-from-type type)]
      (if (bet-message? message)
        (insert-bet-map-to-db db (map-from-bet type))))))

(def connection (irc/connect "irc.chat.twitch.tv" 6667 (get-username)
                             :pass (get-token)
                             :username (get-username)
                             :callbacks {:privmsg privmsg-callback}))

(defn -main
  [& args]
  (println "creating database table")
  (create-db-table)
  (println "starting irc connection")
  (irc/join connection "#saltyteemo")
  )


(comment 
(irclj.events/fire connection :privmsg test-bet-blue)

(irc/join connection "#saltyteemo")

(irc/kill connection)

; https://gist.github.com/rboyd/5053955
(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn generate-test-map
  []
  (let [bettor-name (rand-str 20)
        bet-size (+ (rand-int 10000) 3)
        bet-team (rand-nth ["BLUE" "RED"])]
    {
     :user "xxsaltbotxx"
     :text (str "@" bettor-name " - Bet complete for " bet-team ", " bet-size ". You new balance is 1234.")
     }))

(defn generate-test-data
  [num-cases]
  (repeatedly num-cases generate-test-map))

(generate-test-data 2)
(map (fn [test-type] (irclj.events/fire connection :privmsg test-type)) (generate-test-data 5))

; some fields elided from test
; {:command "PRIVMSG", :params ["#saltyteemo" "@Princess_Pwny - Bet complete for BLUE, 442. Your new balance is 1808153."], :raw ":xxsaltbotxx!xxsaltbotxx@xxsaltbotxx.tmi.twitch.tv PRIVMSG #saltyteemo :@Princess_Pwny - Bet complete for BLUE, 442. Your new balance is 1808153.", :nick "xxsaltbotxx", :user "xxsaltbotxx", :host "xxsaltbotxx.tmi.twitch.tv", :target "#saltyteemo", :text "@Princess_Pwny - Bet complete for BLUE, 442. Your new balance is 1808153."}
(def test-bet-blue {
                     :command "PRIVMSG"
                     :params ["#saltyteemo" "@Princess_Pwny - Bet complete for BLUE, 442. Your new balance is 1808153."]
                     :nick "xxsaltbotxx"
                     :user "xxsaltbotxx"
                     :host "xxsaltbotxx.tmi.twitch.tv"
                     :target "#saltyteemo"
                     :text "@Princess_Pwny - Bet complete for BLUE, 442. Your new balance is 1808153."
                     })

(def blue-bet-message (message-from-type test-bet-blue))

; {:command "PRIVMSG", :params ["#saltyteemo" "@boom0579 - Bet complete for RED, 5000. Your new balance is 17733."], :raw ":xxsaltbotxx!xxsaltbotxx@xxsaltbotxx.tmi.twitch.tv PRIVMSG #saltyteemo :@boom0579 - Bet complete for RED, 5000. Your new balance is 17733.", :nick "xxsaltbotxx", :user "xxsaltbotxx", :host "xxsaltbotxx.tmi.twitch.tv", :target "#saltyteemo", :text "@boom0579 - Bet complete for RED, 5000. Your new balance is 17733."}
(def test-bet-red {
                    :command "PRIVMSG"
                    :params ["#saltyteemo" "@boom0579 - Bet complete for RED, 5000. Your new balance is 17733."]
                    :nick "xxsaltbotxx"
                    :user "xxsaltbotxx"
                    :host "xxsaltbotxx.tmi.twitch.tv"
                    :target "#saltyteemo"
                    :text "@boom0579 - Bet complete for RED, 5000. Your new balance is 17733."
                    })

(def red-bet-message (message-from-type test-bet-red))
)
