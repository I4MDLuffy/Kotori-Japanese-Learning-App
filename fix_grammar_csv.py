# -*- coding: utf-8 -*-
"""
Rewrites grammar entries g251-g280 with proper CSV quoting.
Run from any directory: python fix_grammar_csv.py
"""
import csv, io, os

CSV_PATH = r"C:\Users\ARSRa\AndroidStudioProjects\PersonalProject\app\src\commonMain\composeResources\files\grammar.csv"

# ── Corrected entries (15 fields each) ────────────────────────────────────────
# id, lesson_number, title, content, example_one, example_two,
# supporting_content, jlpt_level, category,
# related_grammar_references, related_kanji_references, related_vocab_references,
# difficulty_order, unlocks_content, origin
FIXED = [
["g251","251","Even If / No Matter How — ~ても / ~でも",
 "Verb て-form plus も, or adjective / noun plus でも, expresses even if or even though. The result in the main clause holds regardless of the condition. Signals that the outcome does not change even if the stated situation is true.",
 "雨が降っても、試合は続ける。(あめがふっても、しあいはつづける。) — Even if it rains, the game will continue.",
 "どんなに疲れていても、諦めない。(どんなにつかれていても、あきらめない。) — No matter how tired I am, I won't give up.",
 "ても / でも = concessive condition (outcome unchanged). Compare: ば (standard conditional — result expected); のに (concessive with disappointment); ても is neutral about speaker attitude. どんなに / いくら + ても reinforces the \"no matter how\" meaning.",
 "N4","condition","","","","251","g252","user-title-ai-content"],

["g252","252","Without Doing — ~ないで / ~ずに",
 "Negative plain form plus で (ないで) or verb stem plus ずに means without doing. Both express that an action is not performed while doing something else. ずに is slightly more formal/literary; ないで is the everyday spoken form.",
 "朝ごはんを食べないで学校へ行った。(あさごはんをたべないでがっこうへいった。) — I went to school without eating breakfast.",
 "何も言わずに部屋を出て行った。(なにもいわずにへやをでていった。) — She left the room without saying anything.",
 "ないで = don't/without (common spoken); ずに = without (formal/written). Negative of する is せずに (not しずに). Compare: てから (after doing); before vs. without nuance differs based on context.",
 "N4","manner","","","","252","g253","user-title-ai-content"],

["g253","253","Should / Ought To — ~べきだ / ~べきではない",
 "Verb plain form (dictionary form) plus べきだ expresses a strong moral or logical obligation: should / ought to. The negative べきではない / べきでない means should not. する becomes すべきだ. More prescriptive than た方がいい.",
 "約束は守るべきだ。(やくそくはまもるべきだ。) — One should keep promises.",
 "他人の悪口を言うべきではない。(たにんのわるぐちをいうべきではない。) — You should not speak ill of others.",
 "べきだ = normative obligation (morally/logically should). Compare: た方がいい (advice — gentler); なければならない (must — obligation from rules or necessity); ほうがいい (suggestion — even gentler).",
 "N3","obligation","","","","253","g254","user-title-ai-content"],

["g254","254","Tendency / Prone To — ~がちだ / ~がち",
 "Verb stem or noun plus がちだ means tends to / is prone to. Usually has a negative or undesirable connotation — a habitual or natural tendency toward something unfavorable. Can be used attributively as がちな (noun).",
 "彼は遅刻しがちだ。(かれはちこくしがちだ。) — He tends to be late.",
 "冬は風邪をひきがちです。(ふゆはかぜをひきがちです。) — In winter one tends to catch colds.",
 "がち = tendency (often negative). Compare: ことが多い (often happens — neutral/positive possible); やすい (easy to / prone to — less negative); っぽい (seeming like / tending to — casual). がちな modifies nouns (遅刻がちな人 = a person who tends to be late).",
 "N3","tendency","","","","254","g255","user-title-ai-content"],

["g255","255","Hard To / Easy To — ~にくい / ~やすい",
 "Verb stem plus にくい means hard to / difficult to. Verb stem plus やすい means easy to / prone to. Both attach like い-adjectives and describe the inherent difficulty or ease of performing the action.",
 "この問題は解きにくい。(このもんだいはときにくい。) — This problem is hard to solve.",
 "彼女の話は聞きやすい。(かのじょのはなしはききやすい。) — Her speech is easy to listen to.",
 "にくい = hard to (inherent difficulty). やすい = easy to / prone to (positive or negative tendency: 怒りやすい = easily angered). Compare: がちだ (tendency — usually negative habit); づらい (hard to — subjective difficulty, nuance of reluctance or psychological resistance).",
 "N4","degree","","","","255","g256","user-title-ai-content"],

["g256","256","Appears To / Seems To — ~ようだ / ~ようです",
 "Plain-form clause plus ようだ (or ようです — polite) expresses a conclusion based on direct observation or experience: it appears that / it seems that. The speaker has sensory evidence and is making an inference. よう can also be used to form similes.",
 "空が暗くなってきた。雨が降るようだ。(そらがくらくなってきた。あめがふるようだ。) — The sky has darkened. It appears it will rain.",
 "彼女は眠そうだ。(かのじょはねむそうだ。) — She looks sleepy.",
 "ようだ = inference from observation. Compare: らしい (inference from hearsay/indirect evidence); そうだ (hearsay: I heard that / appearance: looks like); みたいだ (casual equivalent of ようだ). Simile use: まるで夢のようだ = it's just like a dream.",
 "N4","inference","","","","256","g257","user-title-ai-content"],

["g257","257","Heard That / I Understand That — ~そうだ (hearsay)",
 "Plain-form clause plus そうだ expresses information obtained from an external source: I heard that / it is said that. Distinct from appearance そうだ (verb stem + そうだ). The hearsay form attaches to the plain form of verbs and adjectives.",
 "明日は雨が降るそうだ。(あしたはあめがふるそうだ。) — I heard it will rain tomorrow.",
 "彼は来ないそうです。(かれはこないそうです。) — I understand that he won't come.",
 "Plain-form + そうだ = hearsay (information from others). Do NOT confuse with stem + そうだ = looks like / appearance. Compare: らしい (similar meaning but softer and based on overall impression); によると (according to — introduces source); という話だ (I heard the story that).",
 "N4","hearsay","","","","257","g258","user-title-ai-content"],

["g258","258","Since / Because (Reason) — ~ため(に) / ~ために",
 "Plain-form clause or noun plus ため(に) expresses reason or cause in formal/written contexts: because of / due to. For volitional purpose, dictionary-form verb plus ために expresses in order to. Context determines which meaning applies.",
 "病気のために、会議を欠席した。(びょうきのために、かいぎをけっせきした。) — Due to illness, I was absent from the meeting.",
 "健康のために、毎日運動している。(けんこうのために、まいにちうんどうしている。) — I exercise every day for the sake of my health.",
 "ために (reason) = formal; appears in written/news Japanese. Compare: から (casual reason); ので (softer, explanatory reason); ため is neutral in register. ために (purpose) requires volitional verb: 勉強するために = in order to study.",
 "N3","reason","","","","258","g259","user-title-ai-content"],

["g259","259","In Spite Of / Although — ~にもかかわらず",
 "Noun or plain-form clause plus にもかかわらず means despite / in spite of / notwithstanding. A formal expression that signals a contrast between the given condition and an unexpected result.",
 "雨にもかかわらず、試合は行われた。(あめにもかかわらず、しあいはおこなわれた。) — Despite the rain, the match took place.",
 "彼の反対にもかかわらず、計画は実行された。(かれのはんたいにもかかわらず、けいかくはじっこうされた。) — Notwithstanding his objection, the plan was carried out.",
 "にもかかわらず = formal concessive (in spite of). Compare: のに (concessive — speaker's disappointment or surprise); ても (even if — not necessarily in spite of); にもかかわらず is written/formal, unlike ても. Related: にかかわらず (regardless of — removes the も nuance).",
 "N2","concession","","","","259","g260","user-title-ai-content"],

["g260","260","Regardless Of — ~にかかわらず / ~を問わず",
 "Noun plus にかかわらず or を問わず means regardless of / irrespective of. Used to state that the outcome or situation is the same no matter what X is. を問わず is slightly more formal and often appears in public notices.",
 "経験の有無にかかわらず、応募できます。(けいけんのうむにかかわらず、おうぼできます。) — You can apply regardless of experience.",
 "国籍を問わず、参加できます。(こくせきをとわず、さんかできます。) — Anyone can participate, regardless of nationality.",
 "にかかわらず = regardless (neutral); を問わず = regardless / without exception (formal). Compare: にもかかわらず (despite — but the result is surprising); によらず (not based on — similar meaning); でも〜でも (whether X or Y — binary choice).",
 "N2","condition","","","","260","g261","user-title-ai-content"],

["g261","261","Just As / The Way — ~とおりに / ~どおりに",
 "Noun plus どおりに or verb plain/た-form plus とおりに means just as / as expected / in the way that. Expresses conformity to a method, plan, or expectation.",
 "説明のとおりに操作してください。(せつめいのとおりにそうさしてください。) — Please operate it just as the instructions say.",
 "予定どおりに進んでいる。(よていどおりにすすんでいる。) — It is proceeding as scheduled.",
 "とおりに / どおりに = conformity to a model or expectation. Noun + どおり; verb + とおり. Compare: ように (in a way that / so that — purpose or hope); まま (as is — no change). 言ったとおりにする = do as I said.",
 "N3","manner","","","","261","g262","user-title-ai-content"],

["g262","262","Although / While — ~ながら (concessive)",
 "Verb stem plus ながら can express while doing (simultaneous action) OR although / even though (concessive). In the concessive use, the state in the main clause contradicts what would be expected given the ながら clause.",
 "彼は知りながら、何も言わなかった。(かれはしりながら、なにもいわなかった。) — Although he knew, he said nothing.",
 "狭いながらも、居心地のよい部屋だ。(せまいながらも、いごこちのよいへやだ。) — Although small, it is a comfortable room.",
 "ながら (concessive) = although, yet. Appears mainly with い-adjectives or verbs. The ながらも form (with も) adds extra concessive force. Compare: のに (concessive — disappointment/complaint nuance); ても (even if — neutral); ながら simultaneous has a different, separable meaning.",
 "N2","concession","","","","262","g263","user-title-ai-content"],

["g263","263","No Matter What / Whoever — ~ても / ~でも (with interrogative)",
 "Interrogative word (何, 誰, どこ, いつ, どんなに) plus ても / でも expresses no matter what / whoever / wherever. The outcome is the same regardless of the variable. A natural extension of the concessive ても pattern.",
 "誰が来ても、歓迎します。(だれがきても、かんげいします。) — No matter who comes, we'll welcome them.",
 "どこへ行っても、日本語を話す機会がある。(どこへいっても、にほんごをはなすきかいがある。) — Wherever you go, there are opportunities to speak Japanese.",
 "Interrogative + ても/でも = universal concession (no matter X). Compare: いくら〜ても (no matter how much); どんなに〜ても (no matter how); 何〜しても (whatever one does). The interrogative word absorbs the variable.",
 "N3","concession","","","","263","g264","user-title-ai-content"],

["g264","264","On The Verge Of / About To — ~ところだ (with aspects)",
 "Verb dictionary form plus ところだ = about to do (before). Verb て-form plus いるところだ = in the middle of (during). Verb た-form plus ところだ = just finished (after). Three aspect forms with ところ capture stages of an action.",
 "今から出発するところです。(いまからしゅっぱつするところです。) — I'm just about to depart.",
 "今、報告書を書いているところです。(いま、ほうこくしょをかいているところです。) — I'm in the middle of writing the report right now.",
 "するところだ = about to (imminent future); しているところだ = in progress; したところだ = just completed (recent past). Compare: ばかりだ (just did — similar to したところ but emphasizes immediacy); まだ〜ていない (not yet done). ところ can also mean situation.",
 "N4","aspect","","","","264","g265","user-title-ai-content"],

["g265","265","It's Not As If / There's No Reason — ~わけがない",
 "Plain-form clause plus わけがない means there's no way that / it's impossible that. Stronger than ないはずだ. The speaker asserts that something is logically out of the question based on knowledge or common sense.",
 "彼がそんなことをするわけがない。(かれがそんなことをするわけがない。) — There's no way he would do such a thing.",
 "一日でN1に合格できるわけがない。(いちにちでN1にごうかくできるわけがない。) — There's no way you can pass N1 in one day.",
 "わけがない = logical impossibility (strong). Compare: はずがない (should not be — expectation-based); はずはない (slightly softer); ないはずだ (must not be); わけがない is the most categorical of these negations.",
 "N3","negation","","","","265","g266","user-title-ai-content"],

["g266","266","As Soon As — ~たとたんに / ~とたんに",
 "Verb た-form plus とたんに means the instant / the moment that — something happened immediately and often unexpectedly after the previous action. The second event occurs at the very moment the first is completed.",
 "ドアを開けたとたんに、猫が飛び出してきた。(ドアをあけたとたんに、ねこがとびだしてきた。) — The moment I opened the door, the cat jumped out.",
 "外に出たとたんに雨が降り始めた。(そとにでたとたんにあめがふりはじめた。) — The instant I went outside, it started raining.",
 "たとたんに = instant (often unexpected or involuntary second event). Compare: とすぐに (right after — expected sequence); たら (after if/when — more general); やいなや (no sooner than — literary/formal); たとたん tends to imply the change was beyond the speaker's control.",
 "N2","sequence","","","","266","g267","user-title-ai-content"],

["g267","267","No Sooner Than — ~やいなや / ~か〜ないかのうちに",
 "Verb dictionary form plus やいなや means no sooner than / the instant that. Like たとたんに but more literary. か〜ないかのうちに uses the same verb in both positive and negative forms to express a similar immediacy.",
 "彼は席に着くやいなや、話し始めた。(かれはせきにつくやいなや、はなしはじめた。) — No sooner had he taken his seat than he began speaking.",
 "試験が終わるか終わらないかのうちに、答えを見直した。(しけんがおわるかおわらないかのうちに、こたえをみなおした。) — I reviewed the answers almost before the exam ended.",
 "やいなや = highly formal/literary. か〜ないかのうちに = semi-formal. Both express near-simultaneous events. Compare: たとたんに (common usage, similar meaning); てすぐに (right after — neutral); Use やいなや in formal writing, descriptions, literary texts.",
 "N1","sequence","","","","267","g268","user-title-ai-content"],

["g268","268","On Account Of / Due To (negative result) — ~あまり(に)",
 "Noun or plain-form clause plus あまり(に) expresses that an extreme degree of something causes a negative or unexpected result: so much that / out of excess of. The adjective or noun is typically emotional or a degree expression.",
 "嬉しさのあまり、泣いてしまった。(うれしさのあまり、ないてしまった。) — Out of sheer joy, I ended up crying.",
 "心配のあまり、夜も眠れなかった。(しんぱいのあまり、よるもねむれなかった。) — Due to excessive worry, I couldn't sleep at night.",
 "あまり(に) = excess causing a result (often involuntary consequence). Typically combined with emotion nouns: 悲しさ, 喜び, 緊張, 心配. Compare: あまりにも (too much — isolated adverb); ために (reason — neutral); て (cause-result — general use).",
 "N2","cause","","","","268","g269","user-title-ai-content"],

["g269","269","Considering / Taking Into Account — ~にしては / ~にしても",
 "Noun or plain-form clause plus にしては means considering that / for a ~ (the result is unexpected given the premise). にしても means even granting that / even so (concedes the premise but asserts the conclusion anyway).",
 "日本語を学び始めたばかりにしては、上手ですね。(にほんごをまなびはじめたばかりにしては、じょうずですね。) — Considering you just started learning Japanese, you're quite good.",
 "忙しいにしても、返事ぐらいはできるはずだ。(いそがしいにしても、へんじぐらいはできるはずだ。) — Even granting that you're busy, you should at least be able to reply.",
 "にしては = unexpected contrast with standard (better or worse than expected). にしても = granting the condition, still. Compare: にしては (evaluation), にしても (concession); わりには (considering — similar to にしては but with resentment nuance).",
 "N2","contrast","","","","269","g270","user-title-ai-content"],

["g270","270","Once / Now That — ~からには / ~上は",
 "Plain-form or past-form plus からには means now that / since (strong commitment). 上は is a formal literary equivalent. Both express that given the established situation, a logical obligation or determination follows.",
 "日本語を学ぶからには、JLPT N1を目指したい。(にほんごをまなぶからには、N1をめざしたい。) — Now that I'm studying Japanese, I want to aim for N1.",
 "引き受けた上は、最後まで責任を持つべきだ。(ひきうけたうえは、さいごまでせきにんをもつべきだ。) — Now that I've taken it on, I should be responsible to the end.",
 "からには = since / now that (strong personal commitment from the premise). 上は = now that / once (formal/literary, similar). Compare: ので (reason — neutral); から (reason — casual); からには implies speaker's resolution or obligation.",
 "N2","condition","","","","270","g271","user-title-ai-content"],

["g271","271","While / During — ~間 / ~間に",
 "Noun plus の間 or verb て-form plus いる間 means while / during. 間に adds the nuance that another event occurs within that time window. 間 alone focuses on the duration.",
 "彼女が寝ている間、静かにしていた。(かのじょがねているあいだ、しずかにしていた。) — While she was sleeping, I kept quiet.",
 "外出している間に、誰かが電話してきた。(がいしゅつしているあいだに、だれかがでんわしてきた。) — While I was out, someone called.",
 "間 = duration (throughout); 間に = within the duration (something occurs during). Compare: ながら (while doing — simultaneous; same subject); のうちに (within / before a deadline or limit expires). うちに (while still in the state — often with nuance of acting before it's too late).",
 "N4","time","","","","271","g272","user-title-ai-content"],

["g272","272","Before It's Too Late — ~うちに",
 "Verb て-form plus いる or state expression plus うちに means while still / before it's too late. The speaker implies that action should be taken while the current condition still holds — after that, the window may close.",
 "若いうちに、いろいろな経験をしておくべきだ。(わかいうちに、いろいろなけいけんをしておくべきだ。) — While you're young, you should experience many things.",
 "記憶が新しいうちに、メモしておこう。(きおくがあたらしいうちに、メモしておこう。) — While the memory is fresh, let's take notes.",
 "うちに = while still (implying urgency — do it before the state changes). Compare: 間に (while / within — less urgency, just during); ているうちに (while doing — can also imply gradual change); ないうちに (before doing).",
 "N3","time","","","","272","g273","user-title-ai-content"],

["g273","273","Leaving Something In A State — ~ておく / ~てある",
 "Verb て-form plus おく means to do something in advance / leave something done for future purposes (intentional action). Verb て-form plus ある means a state resulting from someone's prior action (often someone else left it that way).",
 "会議の前に資料を準備しておいた。(かいぎのまえにしりょうをじゅんびしておいた。) — I prepared the materials in advance of the meeting.",
 "黒板に予定が書いてある。(こくばんによていがかいてある。) — The schedule has been written on the blackboard (and remains there).",
 "ておく = intentional advance action (for later benefit). てある = resultant state (transitive verb — the object is in that state). Compare: ている (ongoing state — intransitive or process); てしまう (completion — often with regret/finality).",
 "N4","aspect","","","","273","g274","user-title-ai-content"],

["g274","274","Giving / Receiving Favors — ~てあげる / ~てもらう / ~てくれる",
 "Verb て-form plus あげる / もらう / くれる express the direction of a favor. てあげる = do for someone else; てくれる = someone does something for me; てもらう = I receive a favor from someone / have someone do.",
 "友達のために荷物を運んであげた。(ともだちのためににもつをはこんであげた。) — I carried the luggage for my friend.",
 "先生に漢字を教えてもらった。(せんせいにかんじをおしえてもらった。) — I had the teacher teach me kanji.",
 "Direction of benefit: あげる (outward — I/he gives to another); くれる (inward — someone gives to me); もらう (I receive — I had X done for me). Politeness levels: てさしあげる (humble あげる); てくださる (respectful くれる); ていただく (humble もらう).",
 "N4","social","","","","274","g275","user-title-ai-content"],

["g275","275","Permission & Prohibition — ~てもいい / ~てはいけない / ~てはならない",
 "てもいい grants permission (may / it's okay to). てはいけない forbids (must not / may not). てはならない is a stronger/more formal prohibition often used in written rules. All attach to verb て-form.",
 "ここで写真を撮ってもいいですか。(ここでしゃしんをとってもいいですか。) — May I take photos here?",
 "試験中はスマホを使ってはいけない。(しけんちゅうはスマホをつかってはいけない。) — You must not use your smartphone during the exam.",
 "Permission: てもいい / てもかまわない (it's fine). Prohibition: てはいけない (everyday); てはならない (formal rule); てはだめだ (casual prohibition). Obligation: なければならない / なければいけない (must).",
 "N4","modality","","","","275","g276","user-title-ai-content"],

["g276","276","Must / Have To — ~なければならない / ~なければいけない",
 "Verb negative conditional (なければ) plus ならない or いけない means must / have to. ならない is slightly more formal. The contracted spoken form is なきゃ(ならない/いけない). Also: なくてはならない / なくてはいけない (same meaning).",
 "明日までにレポートを出さなければならない。(あしたまでにレポートをださなければならない。) — I must submit the report by tomorrow.",
 "もっと練習しなければいけないと思っている。(もっとれんしゅうしなければいけないとおもっている。) — I think I have to practice more.",
 "Obligation scale: なければならない (formal strong must); なければいけない (common spoken must); なくてはならない/いけない (same but different syntax); べきだ (ought to — moral); た方がいい (should — advice). Colloquial contractions: なきゃ, なくちゃ.",
 "N4","obligation","","","","276","g277","user-title-ai-content"],

["g277","277","Manner — ~ように / ~ような",
 "ように after a plain-form clause or phrase means in a way that / so that / like. ような modifies nouns (like a ~). Both express comparison or purpose-manner. ように can also set goals or prayers.",
 "子どもでも分かるように説明してください。(こどもでもわかるように、せつめいしてください。) — Please explain it in a way that even a child can understand.",
 "夢のような話だ。(ゆめのようなはなしだ。) — It's a dream-like story.",
 "ように (manner/purpose): so that / in order that (often with negative or potential verb). ような (attributive simile): like a / resembling. Compare: ために (purpose — volitional intent); とおりに (as instructed / as planned — conforming to a model).",
 "N3","manner","","","","277","g278","user-title-ai-content"],

["g278","278","Even / Least Expected — ~すら / ~でさえ (N1 emphasis)",
 "Noun plus すら or でさえ marks an extreme or minimum case: even. If even this holds, all stronger cases certainly do (or don't). すら is slightly more literary/emphatic than でさえ.",
 "忙しくて、食事をとる時間すらなかった。(いそがしくて、しょくじをとるじかんすらなかった。) — I was so busy I didn't even have time to eat.",
 "彼女は名前すら知らなかった。(かのじょはなまえすらしらなかった。) — She didn't even know his name.",
 "すら = literary even (surprising minimum). でさえ = spoken even (similar). Compare: さえ (more neutral even); も (also/even — weakest); すらも intensifies further. N1 texts use すら in literary/written contexts.",
 "N1","emphasis","","","","278","g279","user-title-ai-content"],

["g279","279","Potential / Possibility — ~得る / ~得ない",
 "Verb stem plus 得る (うる/える) means can / is possible. Negative 得ない (えない/うりえない) means cannot / is impossible. A formal/literary pattern common in written Japanese and academic discourse.",
 "そのような事故は起こり得る。(そのようなじこはおこりうる。) — Such an accident is possible / can happen.",
 "これ以上の改善は望み得ない。(これいじょうのかいぜんはのぞみえない。) — Further improvement beyond this cannot be hoped for.",
 "得る (える/うる) = formal potential (possibility). Compare: できる (can — ability or permission); かもしれない (might — uncertain possibility); 得る focuses on logical/factual possibility rather than permission. Common pairs: あり得る (could be), 起こり得る (can happen).",
 "N1","modality","","","","279","g280","user-title-ai-content"],

["g280","280","Scope Marker — ~に関して / ~に関する / ~について",
 "について means concerning / about (general). に関して and に関する are more formal: regarding / in relation to. に関する modifies a following noun. に関して typically follows a noun or pronoun.",
 "この問題について、もう少し詳しく教えてください。(このもんだいについて、もうすこしくわしくおしえてください。) — Could you tell me a bit more about this issue?",
 "環境問題に関する報告書をまとめた。(かんきょうもんだいにかんするほうこくしょをまとめた。) — I compiled a report regarding environmental issues.",
 "について = general about/concerning (N3). に関して = regarding (N2, more formal). に関する + noun = formal attributive. Compare: に対して (toward / in contrast to — directional or adversarial); に関しては (regarding, with topic emphasis).",
 "N2","topic","","","","280","","user-title-ai-content"],
]

def main():
    with open(CSV_PATH, 'r', encoding='utf-8') as f:
        lines = f.read().splitlines()

    # Keep lines that are NOT g251-g280
    kept = []
    for line in lines:
        id_field = line.split(',')[0] if ',' in line else line
        if id_field.startswith('g') and id_field[1:].isdigit():
            n = int(id_field[1:])
            if 251 <= n <= 280:
                continue
        kept.append(line)

    # Build output: kept lines + properly quoted new entries
    buf = io.StringIO()
    buf.write('\n'.join(kept))
    buf.write('\n')

    writer = csv.writer(buf, quoting=csv.QUOTE_MINIMAL, lineterminator='\n')
    for row in FIXED:
        writer.writerow(row)

    with open(CSV_PATH, 'w', encoding='utf-8', newline='') as f:
        f.write(buf.getvalue())

    print(f"Done. Rewrote {len(FIXED)} grammar entries with correct quoting.")

if __name__ == '__main__':
    main()
