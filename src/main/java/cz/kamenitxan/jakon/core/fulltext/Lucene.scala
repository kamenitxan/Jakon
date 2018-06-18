package cz.kamenitxan.jakon.core.fulltext

import java.io.File

import cz.kamenitxan.jakon.core.model.JakonObject
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, Field, StringField, TextField}
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory

import scala.language.postfixOps


object Lucene {
	val indexDirectory = "fulltext"



	val index = FSDirectory.open(new File(indexDirectory).toPath)

	val analyzer = new StandardAnalyzer()
	val iwc = new IndexWriterConfig(analyzer)
	iwc.setOpenMode(OpenMode.CREATE_OR_APPEND)

	val writer = new IndexWriter(index, iwc)

	def indexObject(obj: JakonObject): Unit = {
		val doc = new Document()
		doc.add(new TextField("index", "title text", Field.Store.YES))
		// use a string field for isbn because we don't want it tokenized
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
		result.map( d => searcher.doc(d.doc)) toList
	}

	def main(args: Array[String]): Unit = {
		Lucene.indexObject(null)
		Lucene.search("title text")
	}

}
