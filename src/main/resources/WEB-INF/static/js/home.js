/* globals Chart:false, feather:false */
   // @formatter:off
   var options = {
      series: [{name: "Desktops",data: []}],
      chart: {height: 350,type: 'line',zoom: {enabled: false}},
      dataLabels: {enabled: false},
      stroke: {curve: 'straight'},
      title: {
         text: '交易金额(千元)',align: 'left'
      },
      grid: {
         row: {colors: ['#f3f3f3', 'transparent'], opacity: 0.5},
      },
      xaxis: {categories: [],}
   };
   /*[# th:each="mdo : ${dashBoard.marketDailyList}"]*/
        options.series.data.append($mod.amount);
        options.xaxis.categories.append($mod.date);
   /*[/]*/
   var chart = new ApexCharts(document.querySelector("#chart-amount-bg"), options);
   chart.render();
   // @formatter:on