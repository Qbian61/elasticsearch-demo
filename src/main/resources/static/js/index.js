angular.module('app', ['ngResource', 'ngSanitize'])
    /** 主页面 */
    .controller('mainControl', ['$scope', function ($scope) {
        /** 页面初始化 */
        $scope.init = function() {
            $scope.content = './fragments/add-data.html';
        };

        /** 菜单栏点击切换 */
        $scope.tabClick = function(url) {
            $scope.content = url;
        };

    }])
    /** 自定义 Person 服务 */
    .factory('Person', function ($resource) {
        var ctx = window.qbian._ctx;
        return $resource(ctx + '/person', {}, {
            submit: {
                method: 'POST' ,
                url: ctx + '/person' ,
                params : {
                    name: '@name' ,
                    sex : '@sex' ,
                    interest : '@interest'
                }
            } ,
            query: {
                method: 'GET' ,
                url: ctx + '/person/search' ,
                params : {
                    pageNo: '@pageNo',
                    pageSize: '@pageSize' ,
                    sex: '@sex' ,
                    name: '@name' ,
                    interest: '@interest'
                }
            }
        });

    })
    /** 添加数据页面 */
    .controller('addControl', ['$scope', 'Person', function ($scope, Person) {
        /** 初始化参数 */
        $scope.personV = {
            name : '',
            sex : '',
            interest : ''
        };
        /** 提交 */
        $scope.submit = function () {
            if(!$scope.personV.name) {
                alert('name 不能为空！');
                return ;
            }
            if(!$scope.personV.sex) {
                alert('sex 不能为空！');
                return ;
            }
            if(!$scope.personV.interest) {
                alert('interest 不能为空！');
                return ;
            }
            var params = {
                name : $scope.personV.name,
                sex : $scope.personV.sex,
                interest : $scope.personV.interest
            };
            console.info('请求数据：', params);
            Person.submit(params, function (result) {
                console.info('响应数据：', result);
                if(result.code == 0) {
                    alert('提交成功');
                } else {
                    alert('提交失败：' + result.msg);
                }
            });
        };
    }])
    /** 检索页面 */
    .controller('searchControl', ['$scope', '$sce', 'Person', function ($scope, $sce, Person) {
        /** 初始化参数 */
        // $scope.initPageNumb = 0;
        $scope.initPageSize = 9; // 初始化小于10，下一页按钮不可用
        $scope.personV = {
            name : '',
            sex : '',
            interest : '',
            pageNo : 1
        };
        $scope.persons = [];
        /** 搜索按钮点击事件 */
        $scope.search = function () {
            $scope.personV.pageNo = 1;

            query();
        };
        /** 上一页 */
        $scope.prevPage = function () {
            console.info("上一页");

            $scope.personV.pageNo --;

            query();
        };
        /** 下一页 */
        $scope.nextPage = function () {
            console.info("下一页");

            $scope.personV.pageNo ++;

            query();
        };

        function query() {
            var queryArgs = {
                name : $scope.personV.name || '',
                sex : $scope.personV.sex || '',
                interest : $scope.personV.interest || '' ,
                pageNo : $scope.personV.pageNo,
                pageSize : 10
            };
            console.info('请求数据：', queryArgs);
            Person.query(queryArgs, function (result) {
                console.info('响应数据：', result);
                $scope.persons = [];
                if(result.code == 0) {
                    var data = result.data;
                    $scope.initPageSize = data.length;
                    // $scope.persons = data;
                    for(var i = 0, len = data.length; i < len; ++ i) {
                        $scope.persons.push({
                            date : data[i].date,
                            name : data[i].name,
                            sex : data[i].sex,
                            interest : (function (interest) {
                                return $sce.trustAsHtml(interest);
                            })(data[i].interest)
                        });
                    }
                } else {
                    alert('查询失败：' + result.msg);
                }
            });
        }



    }]);
