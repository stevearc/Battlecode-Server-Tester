var analysis_sorter = new TINY.table.sorter('analysis_sorter','analysis_table',{
	headclass:'head', // Header Class //
	ascclass:'asc', // Ascending Class //
	descclass:'desc', // Descending Class //
	evenclass:'evenrow', // Even Row Class //
	oddclass:'oddrow', // Odd Row Class //
	evenselclass:'evenselected', // Even Selected Column Class //
	oddselclass:'oddselected', // Odd Selected Column Class //
	paginate:true, // Paginate? (true or false) //
	size:1000, // Initial Page Size //
	colddid:'coldid', // Columns Dropdown ID (optional) //
  hoverid:'selectedrow',
	sortcolumn:0, // Index of Initial Column to Sort (optional) //
	sortdir:1, // Sort Direction (1 or -1) //
	init:true // Init Now? (true or false) //
});
