package cz.kamenitxan.jakon.core.fulltext

import scala.language.postfixOps


object Lucene {
	/*val indexDirectory = "fulltext"
	val index = FSDirectory.open(new File(indexDirectory).toPath)
	val analyzer = new StandardAnalyzer()
	val iwc = new IndexWriterConfig(analyzer)
	iwc.setOpenMode(OpenMode.CREATE_OR_APPEND)
	val writer = new IndexWriter(index, iwc)

	def dropIndex(): Unit = {
		writer.deleteAll()
		writer.commit()
	}

	def indexObject(obj: JakonObject): Unit = {
		val indexedText = obj.getClass.getFields.filter(f => {
			val ann = f.getAnnotation(classOf[JakonField])
			ann != null && ann.searched()
		}).map(f => f.get(obj).toString).fold("") { (acc, f) => acc + " " + f }

		val doc = new Document()
		doc.add(new TextField("index", indexedText, Field.Store.YES))
		// use a string field because we don't want it tokenized
		doc.add(new StringField("jakonId", obj.id toString, Field.Store.YES))
		writer.addDocument(doc)
	}


	def search(queryString: String): List[Document] = {
		val reader = DirectoryReader.open(writer)
		val searcher = new IndexSearcher(reader)

		val analyzer = new StandardAnalyzer
		/*val queryBuilder = new PhraseQuery.Builder()
		queryBuilder.add(new Term("index", queryString))
		queryBuilder.build()

		val query = queryBuilder.build()
		val result = searcher.search(query, 10)*/

		val query = new QueryParser("index", analyzer).parse(queryString)
		val result = searcher.search(query, 10).scoreDocs
		result.map(d => searcher.doc(d.doc)) toList
	}
*/
}
